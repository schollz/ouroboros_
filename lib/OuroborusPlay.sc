OuroborusPlay {
	var server;
	var <mapBus;
	var busOut;
	var params;
	var synPlay;
	var bufCurrent;

	*new {
		arg argServer,argBusOut;
		^super.new.init(argServer,argBusOut);
	}


	init {
		arg argServer,argBusOut;
		server=argServer;
		busOut=argBusOut;

		mapBus=Dictionary.new();
		params=Dictionary.newFrom([
			\amp, 1.0,
			\sampleStart, 0,
			\sampleEnd, 1,
			\samplePos, 0,
			\latency, 0,
			\rate, 1,
			\bpm_sample, 120,
			\bpm_target, 120,
			\bitcrush, 0,
			\bitcrush_bits, 4,
			\bitcrush_rate, 4000,
			\scratch, 0,
			\scratchrate, 3,
			\strobe, 0,
			\stroberate, 10,
			\timestretch, 0,
			\timestretch_slow, 4,
			\timestretch_beats, 8,
			\pan, 0,
			\lpf, 20000,
			\hpf, 10,
			\xfade, 0.2;
		]);

		params.keysValuesDo({
			arg key,val;
			mapBus.put(key,Bus.control(server,1));
			mapBus.at(key).set(val);
		});
	}

	play {
		arg samplePos,sampleStart,sampleEnd;
		if (synPlay.notNil,{
			synPlay.set(\t_kill,1);
		});
		params.put(\sampleStart,sampleStart);
		params.put(\sampleEnd,sampleEnd);
		params.put(\samplePos,samplePos);
		synPlay=Synth.new("defOuroborusPlay",[
			\bufnum,bufCurrent,
			\ampBus, mapBus.at(\amp),
			\sampleStart,sampleStart,
			\sampleEnd, sampleEnd,
			\samplePos, samplePos,
			\latencyBus, mapBus.at(\latency),
			\rateBus, mapBus.at(\rate),
			\bpm_sampleBus, mapBus.at(\bpm_sample),
			\bpm_targetBus, mapBus.at(\bpm_target),
			\bitcrushBus, mapBus.at(\bitcrush),
			\bitcrush_bitsBus, mapBus.at(\bitcrush_bits),
			\bitcrush_rateBus, mapBus.at(\bitcrush_rate),
			\scratchBus, mapBus.at(\scratch),
			\scratchrateBus, mapBus.at(\scratchrate),
			\strobeBus, mapBus.at(\strobe),
			\stroberateBus, mapBus.at(\stroberate),
			\timestretchBus, mapBus.at(\timestretch),
			\timestretch_slowBus, mapBus.at(\timestretch_slow),
			\timestretch_beatsBus, mapBus.at(\timestretch_beats),
			\panBus, mapBus.at(\pan),
			\lpfBus, mapBus.at(\lpf),
			\hpfBus, mapBus.at(\hpf),
			\xfadeBus, mapBus.at(\xfade),
		]);
		synPlay.set(\t_trig,1);
	}

	playNew {
		arg buf,samplePos,sampleStart,sampleEnd;
		bufCurrent=buf.bufnum;
		if (synPlay.notNil,{
			synPlay.set(\t_kill,1);
		});
		params.put(\sampleStart,sampleStart);
		params.put(\sampleEnd,sampleEnd);
		params.put(\samplePos,samplePos);
		synPlay=Synth.new("defOuroborusPlay",[
			\bufnum,bufCurrent,
			\ampBus, mapBus.at(\amp),
			\sampleStart,sampleStart,
			\sampleEnd, sampleEnd,
			\samplePos, samplePos,
			\latencyBus, mapBus.at(\latency),
			\rateBus, mapBus.at(\rate),
			\bpm_sampleBus, mapBus.at(\bpm_sample),
			\bpm_targetBus, mapBus.at(\bpm_target),
			\bitcrushBus, mapBus.at(\bitcrush),
			\bitcrush_bitsBus, mapBus.at(\bitcrush_bits),
			\bitcrush_rateBus, mapBus.at(\bitcrush_rate),
			\scratchBus, mapBus.at(\scratch),
			\scratchrateBus, mapBus.at(\scratchrate),
			\strobeBus, mapBus.at(\strobe),
			\stroberateBus, mapBus.at(\stroberate),
			\timestretchBus, mapBus.at(\timestretch),
			\timestretch_slowBus, mapBus.at(\timestretch_slow),
			\timestretch_beatsBus, mapBus.at(\timestretch_beats),
			\panBus, mapBus.at(\pan),
			\lpfBus, mapBus.at(\lpf),
			\hpfBus, mapBus.at(\hpf),
			\xfadeBus, mapBus.at(\xfade),
		]);
		synPlay.set(\t_trig,1);
	}


	free {
		if (synPlay.notNil,{
			synPlay.free;
		});
		params.keysDo({arg key;
			mapBus.at(key).free;
		});
	}
}