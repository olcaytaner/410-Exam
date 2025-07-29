states:q0 q1 q_accept q_reject
input_alphabet: 0 1
REJECT: q_reject
accept: q_accept
start: q0
tape_alphabet: 0 1 _


transitions:
q0 0 -> q1 0 R
q0 1 -> q0 1 R
q0 _ -> q_accept _ R
q1 0 -> q0 0 R
q1 1 -> q1 1 R




