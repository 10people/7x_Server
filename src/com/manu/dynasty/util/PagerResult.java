package com.manu.dynasty.util;

import java.util.List;

/**
 * 分页处理类
 */
public class PagerResult implements java.io.Serializable {

	public int pageNo;//页码
	public int pageSize = 10;//每页显示的行数
	public int total;//总记录数

	public List<? extends Object> datas;

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		if (pageNo < 0)
			pageNo = 0;
		this.pageNo = pageNo;
	}

	public int getPageSize() {

		return pageSize;
	}

	public void setPageSize(int pageSize) {
		if (pageSize <= 0)
			pageSize = 10;
		this.pageSize = pageSize;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public List<? extends Object> getDatas() {
		return datas;
	}

	public void setDatas(List<? extends Object> datas) {
		this.datas = datas;
	}

	public int getTotalPage() {
		return (this.total / this.pageSize + (this.total % this.pageSize == 0 ? 0
				: 1));
	}

}
