(s.waitForBoot{
	"ready".postln;
	m=Ouroborus(Server.default,0);
})

m.recStart(2.0,0.2);

m.recPrime(2.0,0.1);

(a={
	PlayBuf.ar(2,m.bufLoop,loop:1);
}.play)
a.free;


m.bufLoop.plot