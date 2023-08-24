module jexten.examples.plugina {
	exports jexten.examples.plugina;
	requires jexten;
	requires jexten.examples;
	provides jexten.examples.Greeting with jexten.examples.plugina.GreeterA;
}