package com.automate.client.managers.command;

import java.util.List;

public interface ArgumentRange <V> {

	public boolean isValueInRange(V object);
	
	public class NumericRange<N extends Number> implements ArgumentRange <N> {

		private N mLower;
		private N mUpper;
		
		public NumericRange(N mLower, N mUpper) {
			this.mLower = mLower;
			this.mUpper = mUpper;
		}
		public N getLowerBound() {
			return mLower;
		}
		public N getUpperBound() {
			return mUpper;
		}
		
		@Override
		public boolean isValueInRange(N object) {
			return false;
		}	
	}
	
	public class EnumRange implements ArgumentRange<String> {
		
		private List<String> values;

		public EnumRange(List<String> values) {
			this.values = values;
		}

		@Override
		public boolean isValueInRange(String object) {
			return values.contains(object);
		}
		
		public List<String> enumerate() {
			return values;
		}
	}
	
	public class TautologyRange implements ArgumentRange<Object> {

		@Override
		public boolean isValueInRange(Object object) {
			return true;
		}
		
	}
	
}
