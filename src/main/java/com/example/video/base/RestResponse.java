package com.example.video.base;

/**
 * @version 3.3.0
 * @description: The type Rest response.
 * Copyright (C), 2020-2024, 武汉思维跳跃科技有限公司
 * @date 2021/5/25 10:45
 */
public class RestResponse<T> {
    private int code;
    private String message;
    private T response;
    private int size;


    /**
     * Instantiates a new Rest response.
     *
     * @param code    the code
     * @param message the message
     */
    public RestResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Instantiates a new Rest response.
     *
     * @param code     the code
     * @param message  the message
     * @param response the response
     */
    public RestResponse(int code, String message, T response) {
        this.code = code;
        this.message = message;
        this.response = response;
    }
    public RestResponse(int code, String message, T response,int size) {
        this.code = code;
        this.message = message;
        this.response = response;
        this.size=size;
    }


    /**
     * Fail rest response.
     *
     * @param code the code
     * @param msg  the msg
     * @return the rest response
     */
    public static RestResponse fail(Integer code, String msg) {
        return new RestResponse<>(code, msg);
    }

    /**
     * Ok rest response.
     *
     * @return the rest response
     */
    public static RestResponse ok() {
        SystemCode systemCode = SystemCode.OK;
        return new RestResponse<>(systemCode.getCode(), systemCode.getMessage());
    }


    /**
     * Ok rest response.
     *
     * @param <F>      the type parameter
     * @param response the response
     * @return the rest response
     */
    public static <F> RestResponse<F> ok(F response) {
        SystemCode systemCode = SystemCode.OK;
        return new RestResponse<>(systemCode.getCode(), systemCode.getMessage(), response);
    }
    public static <F> RestResponse<F> ok(F response,int size) {
        SystemCode systemCode = SystemCode.OK;
        return new RestResponse<>(systemCode.getCode(), systemCode.getMessage(), response,size);
    }

    /**
     * Gets code.
     *
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * Sets code.
     *
     * @param code the code
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * Gets message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets message.
     *
     * @param message the message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets response.
     *
     * @return the response
     */
    public T getResponse() {
        return response;
    }

    /**
     * Sets response.
     *
     * @param response the response
     */
    public void setResponse(T response) {
        this.response = response;
    }
}
