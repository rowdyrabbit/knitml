package com.knitml.validation.context;

public interface PatternEventListener {
	void begin(Object object, KnittingContext context);
	void end(Object object, KnittingContext context);
	boolean desiresRepeats(Object object, KnittingContext context);
}
