Suckless {
	classvar <>operators;
	classvar <>server;

	*boot { arg server = Server.default;
		server.waitForBoot {
			server.meter.window.alwaysOnTop;
			server.scope.window.bounds_(Rect(0, 800, 300, 300)).alwaysOnTop;
			server.plotTree;
			Suckless.server_(server);
			server.sync;
			Suckless.start;
		}
	}

	*initModules {
		"Suckless: Initializing modules... ".post;
		Ndef(\dc, { \in.kr(1)});
		Ndef(\lfnoise0, { LFNoise0.kr(\freq.kr(1)).range(\min.kr(0), \max.kr(1))});
		Ndef(\lfnoise1, { LFNoise1.kr(\freq.kr(1)).range(\min.kr(0), \max.kr(1))});
		Ndef(\lfnoise2, { LFNoise2.kr(\freq.kr(1)).range(\min.kr(0), \max.kr(1))});
		Ndef(\lfpulse, { LFPulse.kr(\freq.kr(1)).range(\min.kr(0), \max.kr(1))});
		Ndef(\lfsaw, { LFSaw.kr(\freq.kr(1)).range(\min.kr(0), \max.kr(1))});
		Ndef(\lftri, { LFTri.kr(\freq.kr(1)).range(\min.kr(0), \max.kr(1))});
		Ndef(\lfosc, { SinOsc.kr(\freq.kr(1)).range(\min.kr(0), \max.kr(1))});
		Ndef(\gverb, { GVerb.ar(\in.ar(0), \room.kr(10), \revtime.kr(3), \damp.kr(0.5), mul:\level.kr(1))});
		Ndef(\delay, { AllpassC.ar(\in.ar(0), 2, \delaytime.kr(0.2), \decaytime.kr(1), mul:\level.kr(1))});
		Ndef(\midicps, {\in.kr(60).midicps});
		Ndef(\outL, { \in.ar }).play(0);
		Ndef(\outR, { \in.ar }).play(1);
		Ndef(\outS, { [\left.ar, \right.ar] }).play;
		Ndef(\outM, { \in.ar!2 }).play;
		Ndef(\sine, { SinOsc.ar(\freq.kr(440), mul:\amp.kr(1))});
		Ndef(\saw, { Saw.ar(\freq.kr(440), mul:\amp.kr(1))});
		Ndef(\pulse, { Pulse.ar(\freq.kr(440), mul:\amp.kr(1))});
		Ndef(\white, { WhiteNoise.ar(\amp.kr(1)) });
		Ndef(\dust, { Dust.ar(\density.kr(10)) });
		Ndef(\eglinen, { EnvGen.kr(Env.linen(\atk.kr(0.01), \sus.kr(1.0), \rel.kr(1)), \gate.kr(1), doneAction:2) });
		// Ndef(\fm7, {
		// 	var sig, env;
		// 	var freq = \freq.kr(440);
		// 	var amp = \amp.kr(1);
		// 	var out = \out.kr(0);
		// 	var amps = Array.fill(6, { |i| (\amp++(i+1)).asSymbol.kr(0)});
		// 	var ctls, mods;
		//
		// 	ctls = Array.fill(6, { |i|
		// 		[freq * (\freq++(i+1)).asSymbol.kr(i+1), 0, (\level++(i+1)).asSymbol.kr(1)];
		// 	});
		//
		// 	mods = Array.fill(6, { |i|
		// 		Array.fill(6, { |n| (\mod++(i+1)++(n+1)).asSymbol.kr(0)});
		// 	});
		//
		// 	FM7.ar(ctls, mods) * amp;
		// });
		"Done initializing modules.".postln;
	}

	*start {
		Suckless.initModules;
		"Suckless: Initializing pre-processor... ".post;
		thisProcess.interpreter.preProcessor = { |code|
			// (sourceOrValue) (param)> (target)  // WARNING!!!: No space between parameter and '>'
			// (number | string) (string | > | =)> string
			var pattern = "(\\w+|[0-9.]+)\\s+(\\w+|[>=])>\\s*(\\w+)";
			var curOffset = 0;
			var regex = code.findRegexp(pattern);
			var patches = regex.clump(4);
			// regex.debug("regex");
			// patches.debug("patches");
			patches.do{ arg p, i;
				var fullMatch = p[0];
				var value = p[1].last;
				var param = p[2].last;
				var module = p[3].last;
				var replaceStr;
				fullMatch[1].debug("match "++i);
				// value.debug("value");
				// param.debug("param");
				// module.debug("module");
				switch (param,
					"=", { replaceStr = "Ndef(%, Ndef(%).source);".format(value, module).replace("(", "(\\") },
					">", { param = \in; },
					"p", { param = \pitch; },
					"f", { param = \freq; },
					"a", { param = \amp; },
					"g", { param = \gate; },
					"t", { param = \t_trig; },
					"c", { param = \clock; },
					"r", { param = \reset; },
				);
				if (replaceStr == nil) {
					if (value.asNumberIfPossible.isFloat || value.asNumberIfPossible.isInteger ) {
						replaceStr = "Ndef(%).set(%, %);".format(module, param, value.asFloat).replace("(", "(\\");
					} {
						replaceStr = "Ndef(%).set(%, Ndef(%));".format(module, param, value).replace("(", "(\\");
					};
				};
				replaceStr.debug("sclang");
				code = code.replaceAt(replaceStr, fullMatch.first + curOffset, fullMatch.last.size);
				curOffset = curOffset + replaceStr.size - fullMatch.last.size
			};
			code;
		};
		"Done initializing pre-processor.".postln;
	}

	*stop {
		"Suckless mode OFF".postln;
		thisProcess.interpreter.preProcessor = nil;
	}
}

// Suckless {
// 	classvar moduledefs;
// 	classvar <> modules;
// 	classvar preprocessor;
//
// 	///
// 	*boot { arg scopeStyle = 2, server = Server.default;
// 		/*server.makeWindow;*/
// 		// server.boot;
// 		server.waitForBoot {
// 			server.meter;
// 			server.scope(2).style_(scopeStyle)
// 			.window.bounds_(Rect( 0, 1024, 330, 312)); // s.meter.width
// 			FreqScope.new(400, 200, 0, server: server);
// 			server.plotTree;
//
// 			server.sync;
//
// 			Suckless.start();
// 		};
// 	}
//
// 	*start {
// 		Suckless.initModuleDefs();
// 		Suckless.moduleDefNames.collect(_.postln);
// 		Suckless.initPreProcessor();
// 	}
//
// 	*initPreProcessor {
// 		"Suckless: Initializing pre-processor... ".post;
// 		preprocessor = true;
// 		thisProcess.interpreter.preProcessor = { |codeBlock|
// 			// if preprocessor is "on" parse the code
// 			if( preprocessor == true ) {
// 				if (codeBlock == "quit") {
// 					preprocessor = false
// 				} {
// 					SucklessPreProcessor.parse(codeBlock);
// 				}
// 			} { // pass it along otherwise
// 				codeBlock;
// 			}
// 		};
// 		"Done".postln;
// 	}
//
// 	*initModuleDefs {
// 		"Suckless: Initializing module defs... ".post;
// 		moduledefs = IdentityDictionary.new;
// 		moduledefs.put(\dc, { \in.kr(1)});
// 		moduledefs.put(\lfnoise0, { LFNoise0.kr(\freq.kr(1)).range(\min.kr(0), \max.kr(1))});
// 		moduledefs.put(\lfnoise1, { LFNoise1.kr(\freq.kr(1)).range(\min.kr(0), \max.kr(1))});
// 		moduledefs.put(\lfnoise2, { LFNoise2.kr(\freq.kr(1)).range(\min.kr(0), \max.kr(1))});
// 		moduledefs.put(\lfpulse, { LFPulse.kr(\freq.kr(1)).range(\min.kr(0), \max.kr(1))});
// 		moduledefs.put(\lfsaw, { LFSaw.kr(\freq.kr(1)).range(\min.kr(0), \max.kr(1))});
// 		moduledefs.put(\lftri, { LFTri.kr(\freq.kr(1)).range(\min.kr(0), \max.kr(1))});
// 		moduledefs.put(\lfosc, { SinOsc.kr(\freq.kr(1)).range(\min.kr(0), \max.kr(1))});
// 		moduledefs.put(\gverb, { GVerb.ar(\in.ar(0), \room.kr(10), \revtime.kr(3), \damp.kr(0.5), mul:\level.kr(1))});
// 		moduledefs.put(\delay, { AllpassC.ar(\in.ar(0), 2, \delaytime.kr(0.2), \decaytime.kr(1), mul:\level.kr(1))});
// 		moduledefs.put(\midicps, {\in.kr(60).midicps});
// 		moduledefs.put(\out, { \in.ar(0)});
// 		moduledefs.put(\outs, { [\left.ar(0), \right.ar(1)]});
// 		moduledefs.put(\sine, { SinOsc.ar(\freq.kr(440), mul:\amp.kr(1))});
// 		moduledefs.put(\saw, { Saw.ar(\freq.kr(440), mul:\amp.kr(1))});
// 		moduledefs.put(\pulse, { Pulse.ar(\freq.kr(440), mul:\amp.kr(1))});
// 		moduledefs.put(\whitenoise, { WhiteNoise.ar(\amp.kr(1)) });
// 		moduledefs.put(\dust, { Dust.ar(\density.kr(10)) });
// 		moduledefs.put(\fm7, {
// 			var sig, env;
// 			var freq = \freq.kr(440);
// 			var amp = \amp.kr(1);
// 			var out = \out.kr(0);
// 			var amps = Array.fill(6, { |i| (\amp++(i+1)).asSymbol.kr(0)});
// 			var ctls, mods;
//
// 			ctls = Array.fill(6, { |i|
// 				[freq * (\freq++(i+1)).asSymbol.kr(i+1), 0, (\level++(i+1)).asSymbol.kr(1)];
// 			});
//
// 			mods = Array.fill(6, { |i|
// 				Array.fill(6, { |n| (\mod++(i+1)++(n+1)).asSymbol.kr(0)});
// 			});
//
// 			sig = FM7.ar(ctls, mods) * amps;
// 			Mix.ar(sig * amp);
// 		});
// 		"Done".postln;
// 	}
//
// 	*addModuleDef { |name, def|
// 		"Adding new module def: %".format(name).postln;
// 		moduledefs.put(name.asSymbol, def);
// 	}
//
// 	*moduleDefNames {
// 		^moduledefs.keys.asSortedList;
// 	}
//
// 	*list {
// 		"Module templates: ".postln;
// 		Suckless.moduleDefNames.value().collect(_.postln);
// 	}
//
// 	*getModuleDef { |name|
// 		^moduledefs.at(name.asSymbol);
// 	}
//
// 	*add { |name, definition|
// 		if (definition.class == Function) {
// 			Suckless.addModuleDef(name, definition);
// 		} {
// 			^Suckless.addNodeProxy(name, definition.asSymbol);
// 		}
// 	}
//
// 	/// \brief Creates a new module from the template
// 	///
// 	/// \param name          Name of the new module
// 	/// \param definition    Name of the module template
// 	*newModule { |name, definition|
// 		^Suckless.addNodeProxy(name, definition);
// 	}
//
// 	*addNodeProxy { |name, def|
// 		"New % module as %".format(def, name).postln;
// 		^Ndef(name.asSymbol, moduledefs[def]);
// 	}
//
// 	*play { |name, channel|
// 		Ndef(name.asSymbol).play(channel);
// 	}
//
// 	*stop { |name|
// 		Ndef(name.asSymbol).stop;
// 	}
//
// 	*clear { |name, fadeTime|
// 		Ndef(name.asSymbol).clear(fadeTime);
// 	}
//
// 	/// \brief Connect one module's output to another module's input.
// 	///
// 	/// \param from           Name of a module sending the signal
// 	/// \param inputname  Name of the input in the receiving module
// 	/// \param to               Name of the module receiving the signal
// 	*connect { |from, inputname, to|
// 		Ndef(to.asSymbol).set(inputname.asSymbol, Ndef(from.asSymbol));
// 	}
//
// 	/// \brief Disconnect anything that is connected to the given input
// 	///
// 	/// \param module           Name of the module
// 	/// \param connection  Name of the parameter
// 	*disconnect { |module, inputname|
// 		Ndef(module.asSymbol).set(inputname.asSymbol, 0);
// 	}
//
// 	/// \brief Sets a parameter of a module
// 	///
// 	/// \param module           Name of the module
// 	/// \param param             Name of the parameter
// 	/// \param value               Value of the parameter
// 	*set { |module, param, value|
// 		Ndef(module.asSymbol).set(param.asSymbol, value);
// 	}
//
// 	/// \brief Prints the controls of the given module with their connections.
// 	///
// 	/// \param module           Name of the module
// 	///
// 	/// \returns Prints a list of inputs with rate, name and value.
// 	*controls { |module|
// 		// "%'s  controls:".format(module).postln;
// 		var controls = ();
// 		Ndef(module.asSymbol).controlNames.do{ |ctl, i|
// 			var name = ctl.name;
// 			var value = ctl.defaultValue;
// 			var rate = ctl.rate;
// 			// find all characters between single quotes in Ndef('...')
// 			if (value.class == Ndef) { value = value.asString.split($')[1] };
// 			controls.put(i, [rate, name, value]);
// 			// [ctl, i].postln;
// 			// [rate, name, value].postln;
// 		};
// 		^controls;
// 	}
// }
//
// Module {
// 	var <> name;
// 	var <> definition;
// 	var <> description;
//
// 	*new { |name, funcOrSymbol, description = ""|
// 		Suckless.add(name, funcOrSymbol);
// 		^super.newCopyArgs(name, funcOrSymbol, description);
// 	}
//
// 	controls {
// 		"% - %".format(name, definition);
// 		Suckless.controls(name);
// 	}
//
// 	connect { |inputname, from|
// 		if (from.class == String) {from = Ndef(from)};
// 		Suckless.connect(from.name, inputname, name);
// 	}
//
// 	disconnect { |inputname|
// 		Suckless.disconnect(name, inputname);
// 	}
//
// 	play {
// 		Suckless.play(name);
// 	}
//
// 	// printOn { | stream |
// 	// 	stream << "%'s  controls:\n".format(name) << Suckless.controls(name).asString;
// 	// }
//
// 	// > {  |that|
// 	// 	^"% > %".format(this, that);
// 	// }
// }

