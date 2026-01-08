package com.example.basestation.result;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

import java.io.Serializable;

/**
 * 接口返回数据
 * @author gxs
 */
@Data
public final class Result implements Serializable {

	private static final long serialVersionUID = -5001822887685035489L;

	// 200 OK
	private static final String MESSAGE_OK = "操作成功！";

	// 400 BAD_REQUEST
	private static final String MESSAGE_BAD_REQUEST = "参数缺失，请检查参数！";

	// 401 UNAUTHORIZED
	private static final String MESSAGE_UNAUTHORIZED = "帐户已过期，请重新登录！";

	// 403 FORBIDDEN
	private static final String MESSAGE_FORBIDDEN = "无操作权限！";

	// 404 NOT_FOUND
	private static final String MESSAGE_NOT_FOUND = "路径不存在，请检查路径是否正确！";

	// 409 CONFLICT
	private static final String MESSAGE_CONFLICT = "数据已存在！";

	// 500 INTERNAL_SERVER_ERROR
	private static final String MESSAGE_INTERNAL_SERVER_ERROR = "操作失败，请联系管理员！";

	/**
	 * 返回代码
	 */
	private Integer code;

	/**
	 * 返回处理消息
	 */
	private String message;
	
	/**
	 * 返回数据对象 data
	 */
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private Object data;

	/**
	 * 构造
	 */
	private Result() {

	}

	public Result(@NonNull HttpStatus code, String message) {
		this(code, message, null);
	}

	public Result(@NonNull HttpStatus code, String message, Object data) {
		this.code = code.value();
		this.message = message;
		this.data = data;
	}

	/**
	 * 200 OK
	 */
	public static Result ok() {
		return Result.ok(MESSAGE_OK);
	}

	public static Result ok(String message) {
		return Result.ok(message, null);
	}

	public static Result ok(Object data) {
		return Result.ok(MESSAGE_OK, data);
	}

	public static Result ok(String message, Object data) {
		return Result.httpStatusOk(message, data);
	}

	private static Result httpStatusOk(String message, Object data) {
		return new Result(HttpStatus.OK, message, data);
	}

	/**
	 * 400 BAD_REQUEST
	 */
	public static Result badRequest() {
		return Result.badRequest(MESSAGE_BAD_REQUEST);
	}

	public static Result badRequest(String message) {
		return Result.httpStatusBadRequest(message);
	}

	private static Result httpStatusBadRequest(String message) {
		return new Result(HttpStatus.BAD_REQUEST, message, null);
	}

	/**
	 * 401 UNAUTHORIZED
	 */
	public static Result unauthorized() {
		return Result.unauthorized(MESSAGE_UNAUTHORIZED);
	}

	public static Result unauthorized(String message) {
		return Result.httpStatusUnauthorized(message);
	}

	private static Result httpStatusUnauthorized(String message) {
		return new Result(HttpStatus.UNAUTHORIZED, message, null);
	}

	/**
	 * 403 FORBIDDEN
	 */
	public static Result forbidden() {
		return Result.forbidden(MESSAGE_FORBIDDEN);
	}

	public static Result forbidden(String message) {
		return Result.httpStatusForbidden(message);
	}

	private static Result httpStatusForbidden(String message) {
		return new Result(HttpStatus.FORBIDDEN, message, null);
	}

	/**
	 * 404 NOT_FOUND
	 */
	public static Result notFound() {
		return Result.notFound(MESSAGE_NOT_FOUND);
	}

	public static Result notFound(String message) {
		return Result.httpStatusNotFound(message);
	}

	private static Result httpStatusNotFound(String message) {
		return new Result(HttpStatus.NOT_FOUND, message, null);
	}

	/**
	 * 409 CONFLICT
	 */
	public static Result conflict() {
		return Result.conflict(MESSAGE_CONFLICT);
	}

	public static Result conflict(String message) {
		return Result.httpStatusConflict(message);
	}

	private static Result httpStatusConflict(String message) {
		return new Result(HttpStatus.CONFLICT, message, null);
	}

	/**
	 * 500 INTERNAL_SERVER_ERROR
	 */
	public static Result internalServerError() {
		return Result.internalServerError(MESSAGE_INTERNAL_SERVER_ERROR);
	}

	public static Result internalServerError(String message) {
		return Result.httpStatusInternalServerError(message);
	}

	private static Result httpStatusInternalServerError(String message) {
		return new Result(HttpStatus.INTERNAL_SERVER_ERROR, message, null);
	}

}