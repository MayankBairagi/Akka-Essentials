package org.akka.essentials.stm.transactor.example1;

import scala.concurrent.stm.Ref;
import scala.concurrent.stm.japi.STM;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.transactor.Coordinated;

public class Account extends UntypedActor {

	LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	String accountNumber;
	//Use the scala STM Ref for state variables that need to 
	//participate in transactions
	Ref.View<Float> balance = STM.newRef(Float.parseFloat("0"));

	public Account(String accNo, float bal) {
		this.accountNumber = accNo;
		balance.set(Float.valueOf(bal));
	}

	@Override
	public void onReceive(Object o) throws Exception {
		if (o instanceof Coordinated) {
			Coordinated coordinated = (Coordinated) o;
			final Object message = coordinated.getMessage();
			if (message instanceof AccountDebit) {
				coordinated.atomic(new Runnable() {
					public void run() {
						AccountDebit accDebit = (AccountDebit) message;
						//check for funds availability
						if (balance.get() > accDebit.getAmount()) {
							float bal = balance.get() - accDebit.getAmount();
							balance.set(Float.valueOf(bal));
						} else {
							throw new IllegalStateException(
									"Insufficient Balance");
						}
					}
				});

			} else if (message instanceof AccountCredit) {
				coordinated.atomic(new Runnable() {
					public void run() {
						AccountCredit accCredit = (AccountCredit) message;
						float bal = balance.get() + accCredit.getAmount();
						balance.set(Float.valueOf(bal));
					}
				});
			}
		} else if (o instanceof AccountBalance) {
			// reply with the account balance
			sender().tell(new AccountBalance(accountNumber, balance.get()));
		} else
			unhandled(o);
	}
}
