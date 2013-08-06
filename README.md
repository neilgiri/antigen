*This project is forked from [antigen](https://github.com/trvrb/antigen). Here, I wanted a simple
SIRS model, so I stripped out all the stuff about virus phenotypes. This should run significantly
faster than Antigen.*

**Epidemic** implements an SIRS epidemiological model, where hosts make contacts trasmitting infection
and also recover from infection.  During the simulation, the genealogy of infection is tracked.

Population structure is implemented in terms of discrete demes.  Contacts within a deme occur
through standard mass action, while contacts between demes occur at some fraction of the rate of
within deme contact.

-------------------------------------------

I haven't made a makefile.  The program can be compiled with:

javac *.java

Then to run the simulation:

java -Xmx1G Epidemic

The `-Xmx1G` is required, because as an individual-based model, the memory requirements are
typically quite large. Each host requires a minimum of 40 bytes of memory.  With 10 million hosts
(used in the default parameters), the equals 400MB.

In addition to hosts and immune histories, the simulation tracks the virus genealogy through
VirusTree.  This is harder to profile, and will continually grow in memory usage throughout the
simulation.  With the default parameters, VirusTree takes 5.5 MB at the end of a simulated year and
may up to 110 MB at the end of the default 20 simulated years.

All the relevant simulation parameters are contained in `Parameters.java`.  Unfortunately, this
means that changing parameter values requires a recompile.

The simulation will output a timeseries of region-specific prevalence and incidence to
`out.timeseries`.  It will also sample viruses periodically and output their geographic locations to
`out.tips` and a tree connecting these samples to `out.branches`.  This file contains pairs of
viruses, child and parent, representing nodes in a genealogy.

If you have Mathematica, you can generate a number of figures from this output by running the
notebook `antigen-analysis.nb`.

-------------------------------------------

Copyright Trevor Bedford 2010-2013. Distributed under the GPL v3.