package com.java.pageX.pagination;

import java.io.Serializable;

import org.apache.ibatis.session.RowBounds;

import com.java.pageX.kits.StringUtils;

/**
 * 简单分页模型
 * </p>
 * 用户可以通过继承 org.apache.ibatis.session.RowBounds实现自己的分页模型<br>
 * 注意：插件仅支持RowBounds及其子类作为分页参数
 * @author:杨京京
 * @QQ:1280025885
 */
public class Pagination extends RowBounds implements Serializable {

	private static final long serialVersionUID = 1L;

	/* 总数 */
	private int total;

	/* 每页显示条数 */
	private int size;

	/* 总页数 */
	private int pages;

	/* 当前页 */
	private int current = 1;

	/* 查询总记录数（默认 true） */
	private boolean searchCount = true;

	/* 查询总数优化（默认 true） */
	private boolean optimizeCount = true;

	/**
	 * <p>
	 * SQL 排序 ORDER BY 字段，例如： id DESC（根据id倒序查询）
	 * </p>
	 * <p>
	 * DESC 表示按倒序排序(即：从大到小排序)<br>
	 * ASC 表示按正序排序(即：从小到大排序)
	 * </p>
	 */
	private String orderByField;

	/**
	 * 是否为升序 ASC（ 默认： true ）
	 */
	private boolean isAsc = true;

	public Pagination() {
		super();
	}

	/**
	 * <p>
	 * 分页构造函数
	 * </p>
	 * 
	 * @param current
	 *            当前页
	 * @param size
	 *            每页显示条数
	 */
	public Pagination(int current, int size) {
		this(current, size, true);
	}

	public Pagination(int current, int size, boolean searchCount) {
		super(offsetCurrent(current, size), size);
		if (current > 1) {
			this.current = current;
		}
		this.size = size;
		this.searchCount = searchCount;
	}

	protected static int offsetCurrent(int current, int size) {
		if (current > 0) {
			return (current - 1) * size;
		}
		return 0;
	}

	public int getOffsetCurrent() {
		return offsetCurrent(this.current, this.size);
	}

	public boolean hasPrevious() {
		return this.current > 1;
	}

	public boolean hasNext() {
		return this.current < this.pages;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
		this.pages = this.total / this.size;
		if (this.total % this.size != 0) {
			this.pages++;
		}
		/**
		 * 当前页大于总页数，当前页设置为第一页
		 */
		/*if (this.current > this.pages) {
			this.current = 1;
		}*/
	}

	public int getSize() {
		return size;
	}

	public int getPages() {
		return pages;
	}

	public int getCurrent() {
		return current;
	}

	public boolean isSearchCount() {
		return searchCount;
	}

	public void setSearchCount(boolean searchCount) {
		this.searchCount = searchCount;
	}

	public boolean isOptimizeCount() {
		return optimizeCount;
	}

	public void setOptimizeCount(boolean optimizeCount) {
		this.optimizeCount = optimizeCount;
	}

	public String getOrderByField() {
		return orderByField;
	}

	public void setOrderByField(String orderByField) {
		if (StringUtils.isNotEmpty(orderByField)) {
			this.orderByField = orderByField;
		}
	}

	public boolean isAsc() {
		return isAsc;
	}

	public void setAsc(boolean isAsc) {
		this.isAsc = isAsc;
	}

	@Override
	public String toString() {
		return "Pagination { total=" + total + " ,size=" + size + " ,pages=" + pages + " ,current=" + current + " }";
	}
}
