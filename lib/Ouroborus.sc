Ouroborus {
	var server;
	var busOut;
	var busMainline;
	var synRec;
	var synMainline;
	var <mapPlay;
	var recID;

	*new {
		arg argServer,argBusOut;
		^super.new.init(argServer,argBusOut);
	}

	init {
		arg argServer,argBusOut;
		server=argServer;
		busOut=argBusOut;
		synRec=OuroborusRec.new(server,busOut);
		mapPlay=Dictionary.new();

		SynthDef("defOuroborusPlay",{
			arg out=0, bufnum, samplePosBus=1.neg, ampBus, t_trig=0, t_kill=0, loop=1,
			sampleStart=0,sampleEnd=1,samplePos=0, latencyBus=0,
			rateBus,bpm_sampleBus=1,bpm_targetBus=1,
			bitcrushBus,bitcrush_bitsBus,bitcrush_rateBus,
			scratchBus,scratchrateBus,strobeBus,stroberateBus,
			timestretchBus,timestretch_slowBus,timestretch_beatsBus,
			panBus,lpfBus,hpfBus,xfadeBus;

			// vars
			var rate;
			var snd,pos,timestretchPos,timestretchWindow;
			var amp=(In.kr(ampBus)-10).dbamp;//bus2
			var xfade=In.kr(xfadeBus);
			var latency=In.kr(latencyBus);
			var bpm_sample=In.kr(bpm_sampleBus);
			var bpm_target=In.kr(bpm_targetBus);
			var rateIn=In.kr(rateBus,1);//bus2
			var bitcrush=In.kr(bitcrushBus);//bus2
			var bitcrush_bits=In.kr(bitcrush_bitsBus);//bus2
			var bitcrush_rate=In.kr(bitcrush_rateBus);//bus2
			var scratch=In.kr(scratchBus);//bus2
			var scratchrate=In.kr(scratchrateBus);//bus2
			var strobe=In.kr(strobeBus);//bus2
			var stroberate=In.kr(stroberateBus);//bus2
			var timestretch=In.kr(timestretchBus);//bus2
			var timestretch_slow=In.kr(timestretch_slowBus);//bus2
			var timestretch_beats=In.kr(timestretch_beatsBus);//bus2
			var pan=In.kr(panBus);//bus2
			var lpf=In.kr(lpfBus);//bus2
			var hpf=In.kr(hpfBus);//bus2
			var frames=BufFrames.ir(bufnum);
			var duration=BufDur.ir(bufnum);
			var durationRate=duration;

			rate = BufRateScale.ir(bufnum) * bpm_target / bpm_sample;
			rate = rate*LinSelectX.kr(EnvGen.kr(Env.new([0,1,1,0],[0.2,2,1]),gate:Changed.kr(rateIn)),[1,rateIn]);
			// scratch effect
			rate = SelectX.kr(scratch,[rate,LFTri.kr(bpm_target/60*scratchrate)],wrap:0);
			durationRate=durationRate/rate;
			pos = Phasor.ar(
				trig:t_trig,
				rate:rate,
				start:((sampleStart*(rate>0))+(sampleEnd*(rate<0)))*frames,
				end:((sampleEnd*(rate>0))+(sampleStart*(rate<0)))*frames,
				resetPos:samplePos*frames
			);
			pos = Select.ar(samplePosBus>1.neg,[pos,In.ar(samplePosBus).mod(durationRate)/durationRate*frames]);

			timestretchPos = Phasor.ar(
				trig:t_trig,
				rate:rate/timestretch_slow,
				start:((sampleStart*(rate>0))+(sampleEnd*(rate<0)))*frames,
				end:((sampleEnd*(rate>0))+(sampleStart*(rate<0)))*frames,
				resetPos:pos
			);
			timestretchWindow = Phasor.ar(
				trig:t_trig,
				rate:rate,
				start:timestretchPos,
				end:timestretchPos+((60/bpm_target/timestretch_beats)/duration*frames),
				resetPos:timestretchPos,
			);

			snd=BufRd.ar(2,bufnum,pos,
				loop:1,
				interpolation:1
			);
			timestretch=Lag.kr(timestretch,2);
			snd=((1-timestretch)*snd)+(timestretch*BufRd.ar(2,bufnum,
				timestretchWindow,
				loop:1,
				interpolation:1
			));

			snd = RLPF.ar(snd,lpf,0.707);
			snd = HPF.ar(snd,hpf);

			// strobe
			snd = ((strobe<1)*snd)+((strobe>0)*snd*LFPulse.ar(60/bpm_target*stroberate));

			// bitcrush
			snd = (snd*(1-bitcrush))+(bitcrush*Decimator.ar(snd,bitcrush_rate,bitcrush_bits));

			// manual panning
			snd = Balance2.ar(snd[0],snd[1],
				pan+SinOsc.kr(60/bpm_target*stroberate,mul:strobe*0.5)
			);
			snd=snd*amp;

			snd=snd*EnvGen.ar(Env.new([0,1],[xfade],-2))*EnvGen.ar(Env.new([1,0],[xfade],-2),t_kill,doneAction:2);

			snd=DelayN.ar(snd,delaytime:Lag.kr(latency));
			Out.ar(out,snd);
		}).send(server);

		SynthDef("defMainline",{
			arg out,sr=44100;
			Out.ar(out,Phasor.ar(1,1.0/sr,0.0,1000.0));
		}).send(server);

		server.sync;

		busMainline=Bus.audio(server,1);
		synMainline=Synth.head(server,"defMainline",[\out,busMainline.index,\sr,server.sampleRate]);
	}

	recPrime {
		arg argID,seconds,xfade;
		synRec.recPrime(seconds,xfade,{
			arg buf, startTime;
			["recPrime finished",buf, startTime].postln;
			if (mapPlay.at(argID).isNil,{
				mapPlay.put(argID,OuroborusPlay.new(server,busOut));
			});
			mapPlay.at(argID).playNew(buf,busMainline.postln,synMainline);
		});
	}

	recStart {
		arg argID,seconds,xfade;
		synRec.recStart(seconds,xfade,{
			arg buf, startTime;
			["recStart finished",buf, startTime].postln;
			if (mapPlay.at(argID).isNil,{
				mapPlay.put(argID,OuroborusPlay.new(server,busOut));
			});
			mapPlay.at(argID).playNew(buf,busMainline);
		});
	}

	// play {
	// 	arg argID,samplePos,sampleStart,sampleEnd;
	// 	mapPlay.at(argID).play(samplePos,sampleStart,sampleEnd);
	// }

	setParam {
		arg argID, key, val;
		if (mapPlay.at(argID).notNil,{
			mapPlay.at(argID).mapBus.at(key.asSymbol).set(val);
		});
	}

	free {
		synRec.free;
		synMainline.free;
		mapPlay.keysValuesDo({arg key,val;
			val.free;
		});
	}
}