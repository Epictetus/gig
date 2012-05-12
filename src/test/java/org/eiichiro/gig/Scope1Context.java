package org.eiichiro.gig;

import org.eiichiro.jaguar.Description;
import org.eiichiro.jaguar.scope.Context;

public class Scope1Context implements Context {

	@Override
	public <T> T get(Description<T> description) {
		return null;
	}

	@Override
	public <T> void put(Description<T> description, T t) {}

}
