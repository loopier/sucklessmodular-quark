Suckless {
	classvar moduledefs;
	classvar <> modules;
	classvar preprocessor;

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
		Suckless.initPreProcessor();
	}

	*initPreProcessor {
		"Suckless: Initializing pre-processor... ".post;
		preprocessor = true;
		thisProcess.interpreter.preProcessor = { |codeBlock|
			// if preprocessor is "on" parse the code
			if( preprocessor == true ) {
				if (codeBlock == "quit") {
					preprocessor = false
				} {
					SucklessPreProcessor.parse(codeBlock);
				}
			} { // pass it along otherwise
				codeBlock;
			}
		};
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
			^Suckless.addNodeProxy(name, definition.asSymbol);
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
	///
	/// \returns Prints a list of inputs with rate, name and value.
	*controls { |module|
		// "%'s  controls:".format(module).postln;
		var controls = ();
		Ndef(module.asSymbol).controlNames.do{ |ctl, i|
			var name = ctl.name;
			var value = ctl.defaultValue;
			var rate = ctl.rate;
			// find all characters between single quotes in Ndef('...')
			if (value.class == Ndef) { value = value.asString.split($')[1] };
			controls.put(i, [rate, name, value]);
			// [ctl, i].postln;
			// [rate, name, value].postln;
		};
		^controls;
	}
}

Module {
	var <> name;
	var <> definition;
	var <> description;

	*new { |name, funcOrSymbol, description = ""|
		Suckless.add(name, funcOrSymbol);
		^super.newCopyArgs(name, funcOrSymbol, description);
	}

	controls {
		"% - %".format(name, definition);
		Suckless.controls(name);
	}

	connect { |inputname, from|
		if (from.class == String) {from = Ndef(from)};
		Suckless.connect(from.name, inputname, name);
	}

	disconnect { |inputname|
		Suckless.disconnect(name, inputname);
	}

	play {
		Suckless.play(name);
	}

	// printOn { | stream |
	// 	stream << "%'s  controls:\n".format(name) << Suckless.controls(name).asString;
	// }

	// > {  |that|
	// 	^"% > %".format(this, that);
	// }
}

SucklessPreProcessor {

	/// \brief Parses a block of Suckless code into valid Supercollider code
	*parse { |code|
		code.split($\n).collect { |line|
			SucklessPreProcessor.parseLine(line);
		}
	}

	/// \brief Parses a line of Suckless code into valid Supercollider code
	*parseLine { |code|
		// code.split($ ).collect { |items|
		// 	items.postln;
		// }
		case
		{code.contains(">")} { SucklessPreProcessor.connect(code.split($>)) }
		{code.contains("=")} { SucklessPreProcessor.set(code.split($=)) }
		{code.contains(".").not} { Suckless.controls(code) };

	}

	*connect { |items|
		items.do { |item|
			var from = items[0].asSymbol;
			var to = items[1].split($.)[0].asSymbol;
			var param = if(items[1].split($.).size > 1) {items[1].split($.)[0].asSymbol;} {\in};
			// Suckless.connect(from, param, to);
			Ndef(to).set(param, from);
		}
	}

	*set { |items|
		if (items[0].contains(".")) {
			SucklessPreProcessor.setParam(items);
		} {
			SucklessPreProcessor.add(items);
		};
	}

	*setParam { |items|
		var module = items[0].asSymbol;
		var param = if(items[0].split($.).size > 1) {items[1].split($.)[0].asSymbol;};
		var value = items[1];
		"set param".postln;
		Suckless.connect(module, param, value);
		// Ndef(module).set(param, value);
	}

	*add { |items|
		var module = items[0].asSymbol;
		var def = items[1];
		"add".postln;
		Suckless.add(module, def);
	}
}