package com.fmgame.exceltoconf;

/**
 * 执行excelt to conf 时产生的异常
 * 
 * @author luowei
 * @date 2017年10月9日 下午7:19:42
 */
public class ParserException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 创建一个异常。
	 */
    public ParserException() {
        super();
    }

    /**
     * 创建一个异常。
     *
     * @param message 异常信息
     */
    public ParserException(String message) {
        super(message);
    }

    /**
     * 创建一个异常。
     *
     * @param message 异常信息
     * @param cause   异常原因
     */
    public ParserException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 创建一个异常。
     *
     * @param cause 异常原因
     */
    public ParserException(Throwable cause) {
        super(cause);
    }

}
