package com.liferoles.rest.JSON.objects.nvd3stats;

public class Nvd3PieChartDataItem {
	private String key;
	private int y;

	public Nvd3PieChartDataItem() {
	}

	public Nvd3PieChartDataItem(String key, int y) {
		this.key = key;
		this.y = y;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Nvd3PieChartDataItem other = (Nvd3PieChartDataItem) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	public String getKey() {
		return key;
	}

	public int getY() {
		return y;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + y;
		return result;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setY(int y) {
		this.y = y;
	}
}
