package ru.yandex.summer.reflectionjdbc.api.db;

/**
 * Created by ashot.karapetyan on 4/29/14.
 */
public class QueriesParseException extends  RuntimeException {

	public QueriesParseException(){
		super();
	}

	public QueriesParseException(String message) {
		super(message);
	}


	public QueriesParseException(String message, Throwable cause) {
		super(message, cause);
	}
}
