states: q0 q1 q2 q3 q4 q_accept q_reject
input_alphabet: a b c
tape_alphabet: a b c x y z _
start: q0
accept: q_accept
reject: q_reject

transitions:
q0 a -> q1 x R
q0 y -> q3 y R
q0 b -> q_reject b R
q0 _ -> q_reject _ R
q1 a -> q1 a R
q1 y -> q1 y R
q1 b -> q2 y L
q1 _ -> q_reject _ R
q2 a -> q2 a L
q2 y -> q2 y L
q2 x -> q0 x R
q3 y -> q3 y R
q3 b -> q4 b L
q3 _ -> q_accept _ R
q4 b -> q4 b L
q4 y -> q4 y L
q4 x -> q4 a L
q4 a -> q4 a L
q4 _ -> q0 _ R