Suckless {
	classvar moduledefs;

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
		moduledefs.put(\outs, { \in.ar(0!2)});
		moduledefs.put(\sine, { SinOsc.ar(\freq.kr(440), mul:\amp.kr(1))});
		moduledefs.put(\saw, { Saw.ar(\freq.kr(440), mul:\amp.kr(1))});
		moduledefs.put(\pulse, { Pulse.ar(\freq.kr(440), mul:\amp.kr(1))});
		moduledefs.put(\whitenoise, { WhiteNoise.ar(\amp.kr(1)) });
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
		moduledefs.put(name.asSymbol, def);
	}

	*moduleDefNames {
		^moduledefs.keys.asSortedList;
	}

	*getModuleDef { |name|
		^moduledefs.at(name.asSymbol);
	}
}