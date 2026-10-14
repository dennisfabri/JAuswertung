# Editor dialogs copy their frame

`JPenaltyAdditor`, `JPenaltyPointsAdditor`, `JZWEditor`, `JTimeEditor` and `JMeanTimeEditor` copy the dialog frame —
buttons, ENTER/ESC key bindings and strategy (`:220-244`, `:223-247`, `:263-287`, `:162-186`; CPD 366/351/168
tokens).

## Done when

A base editor dialog provides the frame.
