The `.bigshot` file format is a concatenation of multiple images, along with some metadata. Since the images themselves are compressed already, the bigshot file is itself not compressed.

# Structure #

  * 8 bytes, being the ASCII literal `"BIGSHOT "`.
  * 16 bytes, being a hexadecimal number right padded with spaces, that is the length of the index data. Example:
```
"            594a"
```
  * Index data
  * File data follows immediately

# Index Data #

The index data is a colon-delimited list on the form _key1_ ':' _start1_ ':' _length1_ ':' _key2_ ... The keys are file names, relative to the archive root without an initial slash, but using a forward slash as path delimiter. The _start_ and _length_ values are numbers encoded as decimal strings. The _start_ value is measured from the end of the index data. Example:

```
0/0_0.jpg:0:21282:0/0_1.jpg:21282:22219:0/0_10.jpg:43501:22704:0/0_11.jpg:
66205:22982:0/0_12.jpg:89187:29421:0/0_13.jpg:118608:33152:0/0_14.jpg:151760:
35569:...
```

Line breaks have been added for formatting - there are none in the file. The file names are on the format: _zoom_ '/' _tileX_ '`_`' _tileY_ _image-format-suffix_.

# File Data #

The file data follows immediately after the index. There is no padding or alignment between the files, and they are copied into the archive as-is without any compression or encoding.