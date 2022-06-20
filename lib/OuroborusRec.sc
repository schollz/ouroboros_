Ouroborus {
	var server;
	var busOut;
	var oscTr;
	var synPrime;
	var synRecord;
	var valId;
	var valLoopFade=0.2;
	var valLoopSeconds=4;
	var <bufLoop;
	var fnXFader;

	*new {
		arg argServer,argBusOut;
		^super.new.init(argServer,argBusOut);
	}

	init {
		arg argServer,argBusOut;
		server=argServer;
		busOut=argBusOut;
		valId=1000000.rand;

		SynthDef("defRecordTrigger",{
			arg threshold=(-50), volume=0.0, id=0;
			var input,onset;
			input = Mix.new(SoundIn.ar([0, 1]))*EnvGen.ar(Env.new([0,1],[0.2]))*10;
			onset = Trig.kr(Coyote.kr(input,fastLag:0.05,fastMul:0.9,thresh:threshold.dbamp,minDur:0.1));
			SendTrig.kr(onset,id,1.0);
			Silent.ar();
		}).send(server);


		SynthDef("defRecordLoop",{
			arg bufnum, delayTime=0.01, recLevel=1.0, preLevel=0.0,t_trig=0,run=0,loop=1;
			var input;
			RecordBuf.ar(
				inputArray: DelayN.ar(SoundIn.ar([0,1]),delayTime),
				bufnum:bufnum,
				recLevel:recLevel,
				preLevel:preLevel,
				run:run,
				trigger:t_trig,
				loop:loop,
				doneAction:2,
			);
		}).send(server);

		// https://fredrikolofsson.com/f0blog/buffer-xfader/
		fnXFader ={|inBuffer, duration= 2, curve= -2, action|
			var frames= duration*inBuffer.sampleRate;
			if(frames>inBuffer.numFrames, {
				"xfader: crossfade duration longer than half buffer - clipped.".warn;
			});
			frames= frames.min(inBuffer.numFrames.div(2)).asInteger;
			Buffer.alloc(inBuffer.server, inBuffer.numFrames-frames, inBuffer.numChannels, {|outBuffer|
				inBuffer.loadToFloatArray(action:{|arr|
					var interleavedFrames= frames*inBuffer.numChannels;
					var startArr= arr.copyRange(0, interleavedFrames-1);
					var endArr= arr.copyRange(arr.size-interleavedFrames, arr.size-1);
					var result= arr.copyRange(0, arr.size-1-interleavedFrames);
					interleavedFrames.do{|i|
						var fadeIn= i.lincurve(0, interleavedFrames-1, 0, 1, curve);
						var fadeOut= i.lincurve(0, interleavedFrames-1, 1, 0, 0-curve);
						result[i]= (startArr[i]*fadeIn)+(endArr[i]*fadeOut);
					};
					outBuffer.loadCollection(result, 0, action);
				});
			});
		};


		oscTr = OSCFunc({ arg msg, time;
			if (msg[2].asInteger==valId.asInteger,{
				if (msg[3].asInteger==1,{
					"recording start from prime".postln;
					if (synPrime.notNil,{
						synPrime.free;
						synPrime=nil;
					});
					if (synRecord.notNil,{
						synRecord.set(\t_trig,1,\run,1,\loop,0);
					});

				});
			});
		},'/tr',server.addr);
	}

	// recStart allocates and immediately starts a new recording
	recStart {
		arg seconds,xfade;
		valLoopSeconds=seconds;
		valLoopFade=xfade;
		if (bufLoop.notNil,{bufLoop.free;});
		Buffer.alloc(server,server.sampleRate*(seconds+valLoopFade),2,{
			arg buf1;
			("recording "+(valLoopSeconds+valLoopFade)+"seconds").postln;
			synRecord=Synth("defRecordLoop",[\bufnum,buf1,\t_trig,1,\run,1,\loop,0]).onFree({
				arg syn;
				var buf2;
				["recorded",buf1].postln;
				if (valLoopFade>0,{
					("doing crossfade").postln;
					fnXFader.value(buf1,valLoopFade,-2,action:{
						arg buf2;
						("done with buffer"+buf1+"and made"+buf2).postln;
						bufLoop=buf2;
					});
				},{
					bufLoop=buf1;
				});
			});
			NodeWatcher.register(synRecord);
		});
	}

	// recPrime allocates and primes a new recording
	recPrime {
		arg seconds,xfade;
		valLoopFade=xfade;
		valLoopSeconds=seconds;
		if (bufLoop.notNil,{bufLoop.free;});
		Buffer.alloc(server,server.sampleRate*(seconds+valLoopFade),2,{
			arg buf1;
			"recording primed".postln;
			synRecord=Synth("defRecordLoop",[\bufnum,buf1]).onFree({
				arg syn;
				["recorded",buf1].postln;
				if (valLoopFade>0,{
					("doing crossfade").postln;
					fnXFader.value(buf1,valLoopFade,-2,action:{
						arg buf2;
						("done with buffer"+buf1+"and made"+buf2).postln;
						bufLoop=buf2;
					});
				},{
					bufLoop=buf1;
				});
			});
			NodeWatcher.register(synRecord);
			synPrime=Synth("defRecordTrigger",[\id,valId]);
		});
	}

	free {
		oscTr.free;
		synPrime.free;
		synRecord.free;
		bufLoop.free;
	}
}