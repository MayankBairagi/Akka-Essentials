package org.akka.essentials.stm.transactor.example1;

public class TransferMsg {

	Float amtToBeTransferred;

	public TransferMsg(Float amt) {
		amtToBeTransferred = amt;
	}

	public Float getAmtToBeTransferred() {
		return amtToBeTransferred;
	}

}
