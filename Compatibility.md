# Compatibility

This is a compilation of files which currently fail when run against Exalt's test harness.

The test harness attempts to decompile a file, recompile it, and compare the output to the original file.

What causes a test to fail?
* (**Error**) An error occurs during compiling or decompiling.
* (**Mismatch**) The file produced after recompiling in Exalt is not identical to the original.

If a file fails this test, there's no telling what Exalt did wrong. It could be a minor issue (text data is in a different order) or something major (produced the wrong code).

There will be **no support** for issues caused by editing a file which is marked as incompatible using Exalt.

## What if I really need to edit a file that's incompatible?
You can use the assembler/disassembler instead: [https://github.com/thane98/exalt-rs](https://github.com/thane98/exalt-rs)

This provides a method to edit raw opcodes. This is inconvenient compared to using Exalt's language, but it can produce correct scripts reliably.

## FE10

### Mismatch
- C0101.cmb
- C0102.cmb
- C0103.cmb
- C0105.cmb
- C0109.cmb
- C0201.cmb
- C0205.cmb
- C0302.cmb
- C0304.cmb
- C0305.cmb
- C0307.cmb
- C0308.cmb
- C0313.cmb
- C0314.cmb
- C0315.cmb
- C0401.cmb
- C0407a.cmb
- C0407e.cmb
- CFINAL.cmb
- ending.cmb
- T01.cmb
- T03.cmb

### Error
- C0407c.cmb
- startup.cmb

## FE11

### Mismatch
- bmap024.cmb
- command.cmb

## FE12

### Mismatch
- 2invaw9a1.cmb
- ajk3at.cmb
- bmap014.cmb
- bmap017.cmb
- bmap303.cmb
- command.cmb
- f9noa4bg.cmb

### Error
- bmap023.cmb
- bmap205.cmb
- bmap206.cmb
- bmap206B.cmb
- recollection.cmb

## FE13

### Mismatch
- aDebug.cmb
- bev_shared.cmb
- Command.cmb
- P001.cmb
- P002.cmb

### Error
- bev.cmb

## FE14

### Mismatch
- bev.cmb
- Command.cmb

## FE15

### Mismatch
- bev.cmb
- Command.cmb
- GMAP.cmb
- ドーマの沼.cmb