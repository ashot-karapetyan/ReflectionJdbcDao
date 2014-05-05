package ru.yandex.summer.reflectionjdbc.api.exception;

/**
 * Created by ashot.karapetyan on 4/29/14.
 */
public class ReflectionJdbcSQLException extends RuntimeException {

	public ReflectionJdbcSQLException(){
		super();
	}

	public ReflectionJdbcSQLException(String message) {
		super(message);
	}

    public ReflectionJdbcSQLException(Throwable throwable) {
        super(throwable);
    }


	public ReflectionJdbcSQLException(String message, Throwable cause) {
		super(message, cause);
	}
}
