Suckless {
	classvar moduledefs;
	classvar <> modules;

	///
	*boot { arg scopeStyle = 2, server = Server.default;
		/*server.makeWindow;*/
		// server.boot;
		server.waitForBoot {
			server.meter;
			server.scope(2).style_(scopeStyle)
			.window.bounds_(Rect( 0, 1024, 330, 312)); // s.meter.width
			FreqScope.new(400, 200, 0, server: server);
			server.plotTree;

			server.sync;

			Suckless.start();
		};
	}

	*start {
		Suckless.initModuleDefs();
		Suckless.moduleDefNames.collect(_.postln);
		// "Suckless: Initializing pre-processor... ".post;
		// thisProcess.interpreter.preProcessor = { |codeBlock|
		// 	codeBlock.split($\n).collect { |code|
		// 		var items = code.split($ );
		// 		items.postln;
		// 		case
		// 		{code.beginsWith("add")} {
		// 			moduledefs.at(items[2].asSymbol).asCompileString.postln;
		// 			moduledefs.at(items[2].asSymbol).asCompileString;
		// 			"Ndef(\\"++items[1]++", "++moduledefs.at(items[2].asSymbol).asCompileString++");";
		// 		}
		// 		{code.beginsWith("play")} {
		// 			"Ndef(\\"++items[1]++").play;";
		// 		}
		// 		{code.beginsWith("stop")} {
		// 			"Ndef(\\"++items[1]++").stop;";
		// 		}
		// 		{code.beginsWith("clear")} {
		// 			"Ndef(\\"++items[1]++").clear;";
		// 		}
		// 		{true} {
		// 			var name = items[0];
		// 			var param = items[1];
		// 			var value = if ("[a-zA-Z]".matchRegexp(items[2])) {
		// 				"Ndef(\\"++items[2]++")";
		// 			} {
		// 				items[2].asFloat
		// 			};
		// 			"Ndef(\\"++name++").set(\\"++param++", "++value++");";
		// 		};
		// 	}
		// 	.join;
		// };
		"Done".postln;
	}

	*initModuleDefs {
		"Suckless: Initializing module defs... ".post;
		moduledefs = IdentityDictionary.new;
		moduledefs.put(\dc, { \in.kr(1)});
		moduledefs.put(\lfnoise0, { LFNoise0.kr(\freq.kr(1)).range(\min.kr(0), \max.kr(1))});
		moduledefs.put(\lfnoise1, { LFNoise1.kr(\freq.kr(1)).range(\min.kr(0), \max.kr(1))});
		moduledefs.put(\lfnoise2, { LFNoise2.kr(\freq.kr(1)).range(\min.kr(0), \max.kr(1))});
		moduledefs.put(\lfpulse, { LFPulse.kr(\freq.kr(1)).range(\min.kr(0), \max.kr(1))});
		moduledefs.put(\lfsaw, { LFSaw.kr(\freq.kr(1)).range(\min.kr(0), \max.kr(1))});
		moduledefs.put(\lftri, { LFTri.kr(\freq.kr(1)).range(\min.kr(0), \max.kr(1))});
		moduledefs.put(\lfosc, { SinOsc.kr(\freq.kr(1)).range(\min.kr(0), \max.kr(1))});
		moduledefs.put(\gverb, { GVerb.ar(\in.ar(0), \room.kr(10), \revtime.kr(3), \damp.kr(0.5), mul:\level.kr(1))});
		moduledefs.put(\delay, { AllpassC.ar(\in.ar(0), 2, \delaytime.kr(0.2), \decaytime.kr(1), mul:\level.kr(1))});
		moduledefs.put(\midicps, {\in.kr(60).midicps});
		moduledefs.put(\out, { \in.ar(0)});
		moduledefs.put(\outs, { [\left.ar(0), \right.ar(1)]});
		moduledefs.put(\sine, { SinOsc.ar(\freq.kr(440), mul:\amp.kr(1))});
		moduledefs.put(\saw, { Saw.ar(\freq.kr(440), mul:\amp.kr(1))});
		moduledefs.put(\pulse, { Pulse.ar(\freq.kr(440), mul:\amp.kr(1))});
		moduledefs.put(\whitenoise, { WhiteNoise.ar(\amp.kr(1)) });
		moduledefs.put(\dust, { Dust.ar(\density.kr(10)) });
		moduledefs.put(\fm7, {
			var sig, env;
			var freq = \freq.kr(440);
			var amp = \amp.kr(1);
			var out = \out.kr(0);
			var amps = Array.fill(6, { |i| (\amp++(i+1)).asSymbol.kr(0)});
			var ctls, mods;

			ctls = Array.fill(6, { |i|
				[freq * (\freq++(i+1)).asSymbol.kr(i+1), 0, (\level++(i+1)).asSymbol.kr(1)];
			});

			mods = Array.fill(6, { |i|
				Array.fill(6, { |n| (\mod++(i+1)++(n+1)).asSymbol.kr(0)});
			});

			sig = FM7.ar(ctls, mods) * amps;
			Mix.ar(sig * amp);
		});
		"Done".postln;
	}

	*addModuleDef { |name, def|
		"Adding new module def: "++name.postln;
		moduledefs.put(name.asSymbol, def);
	}

	*moduleDefNames {
		^moduledefs.keys.asSortedList;
	}

	*list {
		"Module templates: ".postln;
		Suckless.moduleDefNames.value().collect(_.postln);
	}

	*getModuleDef { |name|
		^moduledefs.at(name.asSymbol);
	}

	*add { |name, definition|
		if (definition.class == Function) {
			Suckless.addModuleDef(name, definition);
		} {
			^Suckless.addNodeProxy(name, definition);
		}
	}

	/// \brief Creates a new module from the template
	///
	/// \param name          Name of the new module
	/// \param definition    Name of the module template
	*newModule { |name, definition|
		^Suckless.addNodeProxy(name, definition);
	}

	*addNodeProxy { |name, def|
		"Adding new module: "++name.postln;
		^Ndef(name.asSymbol, moduledefs[def]);
	}

	*play { |name, channel|
		Ndef(name.asSymbol).play(channel);
	}

	*stop { |name|
		Ndef(name.asSymbol).stop;
	}

	*clear { |name, fadeTime|
		Ndef(name.asSymbol).clear(fadeTime);
	}

	/// \brief Connect one module's output to another module's input.
	///
	/// \param from           Name of a module sending the signal
	/// \param inputname  Name of the input in the receiving module
	/// \param to               Name of the module receiving the signal
	*connect { |from, inputname, to|
		Ndef(to.asSymbol).set(inputname.asSymbol, Ndef(from.asSymbol));
	}

	/// \brief Disconnect anything that is connected to the given input
	///
	/// \param module           Name of the module
	/// \param connection  Name of the parameter
	*disconnect { |module, inputname|
		Ndef(module.asSymbol).set(inputname.asSymbol, 0);
	}

	/// \brief Sets a parameter of a module
	///
	/// \param module           Name of the module
	/// \param param             Name of the parameter
	/// \param value               Value of the parameter
	*set { |module, param, value|
		Ndef(module.asSymbol).set(param.asSymbol, value);
	}

	/// \brief Prints the controls of the given module with their connections.
	///
	/// \param module           Name of the module
	*controls { |module|
		"Controls for: "++module.postln;
		Ndef(module.asSymbol).controlNames.do{ |ctl|
			var name = ctl.name;
			var value = ctl.defaultValue;
			var rate = ctl.rate;
			// find all characters between single quotes in Ndef('...')
			if (value.class == Ndef) { value = value.asString.split($')[1] };
			[rate, name, value].postln;
		};
	}
}