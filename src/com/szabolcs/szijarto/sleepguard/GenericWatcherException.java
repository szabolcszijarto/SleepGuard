package com.szabolcs.szijarto.sleepguard;

@SuppressWarnings("serial")
public class GenericWatcherException extends Exception {

	public GenericWatcherException() {
		super();
	}

	public GenericWatcherException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public GenericWatcherException(String detailMessage) {
		super(detailMessage);
	}

	public GenericWatcherException(Throwable throwable) {
		super(throwable);
	}
}
