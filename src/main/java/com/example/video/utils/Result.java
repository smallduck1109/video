package com.example.video.utils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "数据响应数据")
@Data
public class Result<T> {
    @ApiModelProperty(value = "响应码")
    private Integer code;
    @ApiModelProperty(value = "响应信息")
    private String msg;
    @ApiModelProperty(value = "响应数据")
    private T data;

    public static <T> Result success(T data) {
        Result result = new Result();
        result.setCode(200);
        result.setMsg("操作成功！");
        result.setData(data);
        return  result;
    }

    public static Result success() {
        Result result = new Result();
        result.setCode(200);
        result.setMsg("操作成功！");
        return  result;
    }

    public static Result error() {
        Result result = new Result();
        result.setCode(500);
        result.setMsg("操作失败！");
        return  result;
    }

    public static Result error(String msg) {
        Result result = new Result();
        result.setCode(500);
        result.setMsg(msg);
        result.setMsg("操作失败！");
        return  result;
    }

    public static Result to(boolean rs) {
        return rs ? success() : error();
    }

}
