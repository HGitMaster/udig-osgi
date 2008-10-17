UPDATE:

Navigate to:
- http://docs.codehaus.org/spaces/exportspace.action?key=UDIG

1. Export Fomrat: select "HTML Output"
   Other Options: uncheck "Include Comments"
2. Pages to export: Check All
3. Export

This will generate a zip file of the following format:
- UDIG-20041003-18_58_23.zip

Extract this zip file right overtop of this plugin - it will update the
"udig folder".

You may have to update some of the table of contents files.
At somepoint it will be nice to walk through the generated index.html file and
generate the toc information.

TRANSLATION:

The best bet for documentation to find "Locale specific files" in the
Platform Plug-in Developers Guide.

Search order for English Canadian Locale:
1) net.refractions.udig.help/nl/en/CA/
2) net.refractions.udig.fragmenthelp/nl/en/CA/
3) net.refractions.udig.help/nl/en/
4) net.refractions.udig.fragmenthelp/nl/en/
5) net.refractions.udig.help/
6) net.refractions.udig.fragmenthelp/

For GeoConnections we should provide translations for:
  net.refractions.udig.help/nl/fr/

This translation effort should focus on context files and the Users Guide.
When this translation grow large we will separate it out into a fragment.