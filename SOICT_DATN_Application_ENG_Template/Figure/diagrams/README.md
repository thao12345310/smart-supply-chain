# Diagrams (PlantUML)

Source `.puml` files live in `src/`; rendered `.png` files are written here and
embedded by the thesis via `\includegraphics{Figure/diagrams/<name>.png}`.

## Render

```bash
brew install plantuml graphviz     # one-time
bash render.sh                     # renders every src/*.puml -> *.png
```

To render a single file: `plantuml -tpng -o .. src/er_diagram.puml`

For sharper print quality you can render SVG instead: `plantuml -tsvg -o .. src/<name>.puml`
(then include the `.svg` — `graphicx` supports it via the `svg` package or convert to PDF).

## Diagram → thesis section map

| File | Thesis section |
|------|----------------|
| usecase_general | 2.2.1 General use case |
| usecase_purchasing / _sales / _warehouse / _delivery | 2.2.2 Detailed use cases |
| activity_procure_to_stock / activity_order_to_cash | 2.2.3 Business processes |
| architecture | 4.1.1 Architecture selection |
| package_diagram | 4.1.2 Overall design |
| class_inbound / _outbound / _delivery | 4.1.3 Detailed package design |
| class_detail_key | 4.2.2 Layer design (attrs+methods) |
| seq_so_gi_fefo / seq_po_gr / seq_delivery_trip | 4.2.2 Sequence diagrams |
| er_diagram | 4.2.3 Database design |
| state_purchase_order / state_sales_order | supporting (lifecycle) |
| flowchart_fefo | Chapter 5 (FEFO contribution) |
| deployment | 4.5 Deployment |

## Editing
Edit the `.puml` source, not the PNG. The VS Code "PlantUML" extension gives live
preview (Alt+D). After editing, re-run `bash render.sh` and rebuild the thesis.
