# SAI
SAI is a lightweight static analysis and instrumentation framework for Android. A particularity of SAI, compared to other similar frameworks such as Soot and WALA, is that it aims to be executable directly on mobile devices. SAI is still in development, and as such, is not completely functional.

## Usage
```
--assemble                   Assembles the content of the "tmp" folder
--call-graph                 Generates a call-graph of the loaded application (not yet comprenhensive)
--control-flow-graph         Generates the control-flow graph of each method present in the apk
--disassemble [apk file]     Disassembles the apk file in folder "tmp"
--help                       Displays the available options
--inline-monitor             Inlines a simple information-flow control monitor in the disassembled code
--load                       Loads the disassembled application of folder "tmp"
--process-algebra            Generates a CWB-compatible CCS representation of the application to enable model-checking (not yet functional)
```