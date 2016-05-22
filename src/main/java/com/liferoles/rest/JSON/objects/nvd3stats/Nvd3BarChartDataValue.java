package com.liferoles.rest.JSON.objects.nvd3stats;

public class Nvd3BarChartDataValue {
	private String x;
	private int y;
	
	public Nvd3BarChartDataValue(){}
	
	public Nvd3BarChartDataValue(String x, int y){
		this.x=x;
		this.y=y;
	}
	
	public String getX() {
		return x;
	}
	public void setX(String x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Nvd3BarChartDataValue other = (Nvd3BarChartDataValue) obj;
		if (x == null) {
			if (other.x != null)
				return false;
		} else if (!x.equals(other.x))
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BarChartDataValue [x=" + x + ", y=" + y + "]";
	}
}
