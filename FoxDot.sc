FoxDot
{

	classvar server;
	classvar midiout;

	*start
	{

		server = Server.default;

		server.options.memSize = 8192 * 16; // increase this if you get "alloc failed" messages
		server.options.maxNodes = 1024 * 32; // increase this if you are getting drop outs and the message "too many nodes"
		server.options.numOutputBusChannels = 2; // set this to your hardware output channel size, if necessary
		server.options.numInputBusChannels = 2; // set this to your hardware output channel size, if necessary
		server.boot();

		OSCFunc(
			{
				arg msg, time, addr, port;
				var fn;

				// Get local filename

				fn = msg[1].asString;

				// Print a message to the user

				("Loading SynthDef from" + fn).postln;

				// Add SynthDef to file

				fn = File(fn, "r");
				fn.readAllString.interpret;
				fn.close;

			},
			'foxdot'
		);

		StageLimiter.activate(2);

		"Listening for messages from FoxDot".postln;
	}

	*midi
	{
		arg port=0;

		MIDIClient.init;

		midiout = MIDIOut(port);

		OSCFunc(
			{
				arg msg, time, addr, port;
				var note, vel, sus, channel;

				// listen for specific MIDI trigger messages from FoxDot

				note    = msg[2];
				vel     = msg[3];
				sus     = msg[4];
				channel = msg[5];

				Task.new(
					{
						midiout.noteOn(channel, note, vel);
						sus.wait;
						midiout.noteOff(channel, note, vel)

					}
				).play;

			},
			'foxdot_midi'

		);

		("Sending FoxDot MIDI messages to" + MIDIClient.destinations[port].name).postln;

	}
}