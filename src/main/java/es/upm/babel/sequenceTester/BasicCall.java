package es.upm.babel.sequenceTester;

import es.upm.babel.cclib.Tryer;

public abstract class BasicCall extends Tryer implements GetValue {
    Object returnValue;
    Object user;

    public BasicCall() { returnValue = null; }

    public abstract void setController(Object controller);

    public void toTry() throws Throwable { }

    public Object returnValue() {
	return returnValue;
    }

    public void setReturnValue(Object returnValue) {
	this.returnValue = returnValue;
    }

    public boolean returned() {
	return !isBlocked() && !raisedException();
    }

    public void setUser(Object user) {
	this.user = user;
    }

    public Object user() {
	return user;
    }
}
