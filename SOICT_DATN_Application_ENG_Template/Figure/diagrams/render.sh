#!/usr/bin/env bash
# Render all PlantUML sources in src/ to PNG in this folder (Figure/diagrams/).
# Requires: plantuml + graphviz  (brew install plantuml graphviz)
set -e
cd "$(dirname "$0")"
echo "Rendering .puml -> .png ..."
plantuml -tpng -o ".." src/*.puml
echo "Done. PNGs are in: $(pwd)"
ls -1 *.png 2>/dev/null || echo "(no PNGs found - check errors above)"
