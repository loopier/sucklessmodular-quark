Suckless.boot;
(
Suckless.add("stereo", \outs);
Suckless.add(\d1, \dust);
Suckless.add(\d2, \dust);
Suckless.connect(\d1, \left, \stereo);
Suckless.connect(\d2, \right, \stereo);
Suckless.play(\stereo);
)
Suckless.disconnect(\stereo, \left);
Suckless.disconnect(\stereo, \right);
Suckless.clear(\s1);
Suckless.addNodeProxy(\n1, \whitenoise);
Suckless.connect(\d1, \left, \stereo);
Suckless.connect(\d2, \right, \stereo);
Suckless.disconnect(\stereo, \left);
Suckless.list.collect(_.postln)
// Suckless.addNodeProxy(\fm, \fm7);
Suckless.stop(\stereo);
Suckless.clear(\stereo);

Suckless.list
Suckless.controls(\d1)
Suckless.controls(\stereo)

// code concept
s1 = sine
s1 > out(L)

// worksheet
Quarks.gui

Suckless.boot
(
var d1 = Module.new("d1", \dust, "alo");
var d2 = Module.new("d2", \dust, "bye");
var st = Module.new("st", \outs, "Two channel output");
st.connect(\left, d1);
d1.controls;
st.play;
)
Suckless.controls(\d1).postln

Suckless.start
d1 = dust
d1.da = dust
dust
d1