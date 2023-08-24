module jexten.examples {
	requires jexten;
	requires jexten.maven;
	exports jexten.examples;
	opens jexten.examples to jexten;
	uses jexten.examples.Greeting;
}