states: q0 q1 q2 q3 q4 q5 q_accept q_reject
input_alphabet: a b
REJECT: q_reject  
accept: q_accept
start: q0
tape_alphabet: a b _ X Y

transitions:
q0 a -> q1 X R
q0 b -> q2 Y R  
q0 _ -> q_accept _ R
q1 a -> q1 a R
q1 b -> q1 b R
q1 _ -> q3 _ L
q2 a -> q2 a R
q2 b -> q2 b R
q2 _ -> q4 _ L
q3 a -> q5 X L
q3 X -> q_accept _ R
q3 Y -> q_accept _ R
q3 b -> q_reject b R
q4 b -> q5 Y L
q4 X -> q_accept _ R
q4 Y -> q_accept _ R
q4 a -> q_reject a R
q5 a -> q5 a L
q5 b -> q5 b L  
q5 X -> q0 X R
q5 Y -> q0 Y R