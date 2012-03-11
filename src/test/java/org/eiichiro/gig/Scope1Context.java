package org.eiichiro.gig;

import org.eiichiro.jazzmaster.Description;
import org.eiichiro.jazzmaster.scope.Context;

public class Scope1Context implements Context {

	@Override
	public <T> T get(Description<T> description) {
		return null;
	}

	@Override
	public <T> void put(Description<T> description, T t) {}

}
