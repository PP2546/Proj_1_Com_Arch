        lw      0       2       mc          # Load mc into reg2
        lw      0       3       mp          # Load mp into reg3
        lw      0       4       pos1        # Load 1 into reg4 (positive 1)
        lw      0       5       neg1        # Load -1 into reg5 (negative 1)
        lw      0       6       count       # Load exit address into reg6

check   nand    3       4       7           # reg7 = !(reg3 & reg4) (NAND operation)
        nand    7       7       7           # reg7 = !reg7 (NAND operation with itself)
        beq     7       1       plus        # If reg7 is 1 (means reg2 is set), go to plus
        beq     7       0       shift       # If reg7 is 0 (means reg2 is not set), go to loop2

plus    add     1       2       1           # reg1 = reg1 + reg2 (accumulate mc)

shift   add     2       2       2           # reg2 = reg2 + reg2 (left shift mc)
        add     4       4       4           # reg4 = reg4 + reg4 (left shift pos1)
        add     6       5       6           # reg6 = reg6 - 1 (decrement exit counter)
        beq     6       0       end         # If reg6 is 0, go to end
        beq     0       0       check       # Go back to check
        noop                                # No operation (placeholder)

end     halt                                # End of program

mc      .fill   32766                       # Multiplicand value (32766)
mp      .fill   10383                       # Multiplier value (10383)
pos1    .fill   1                           # Positive 1
neg1    .fill   -1                          # Negative 1
count   .fill   15                          # counter