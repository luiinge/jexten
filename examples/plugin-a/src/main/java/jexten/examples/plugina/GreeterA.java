package jexten.examples.plugina;

import jexten.Extension;
import jexten.examples.Greeting;

@Extension
public class GreeterA implements Greeting {

	@Override
	public String greet() {
		return "Hello from Plugin A!";
	}

}
