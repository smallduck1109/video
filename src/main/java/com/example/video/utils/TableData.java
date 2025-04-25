package com.example.video.utils;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("分页数据")
public class TableData<T> {

    @ApiModelProperty(value = "状态码")
    private Integer code;
    @ApiModelProperty(value = "提示信息")
    private String msg;
    @ApiModelProperty(value = "数据")
    private List<T> rows;
    @ApiModelProperty(value = "总记录数")
    private Long total;

    public static TableData success(List list, Long total) {
        TableData tableData = new TableData();
        tableData.setCode(200);
        tableData.setMsg("操作成功！");
        tableData.setRows(list);
        tableData.setTotal(total);
        return tableData;
    }

}
