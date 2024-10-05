        lw      0       1       n 			; Load reg1 = n
        lw      0       2       r 			; Load reg2 = r
        lw      0       4       stack 		; Load reg4 = stack
        lw      0       5       return 		; Load reg5 = return

check   beq     1       2       addone 		; check if n = r go to add 1 on reg3
        beq     2       0       addone      ; else     r = 0 go to add 1 on reg3
        lw      0       6       neg1 		; reg6 = -1
        add     1       6       1 			; n -= 1
        lw      0       6       pos1 		; reg6 = 1
        sw      4       5       stack 		; store return on stack

        add     6       4       4 			; stack += 1
        sw      4       1       stack 		; store n -> stack
        add     6       4       4 			; stack += 1
        sw      4       2       stack 		; store r -> stack
        add     6       4       4 			; ++ stack
        lw      0       6       index 		; reg6 = index
        jalr    6       5 			        ; jump to check >> reg6 = index >> reg5 = return
        add     3       0       7 			; reg7 = register3
        lw      0       6       pos1 		; reg6 = 1
        sw      4       7       stack 		; store reg7 -> stack
        lw      0       6       neg1 		; reg6 = -1

        add     6       4       4 			; -- stack
        lw      4       2       stack 		; pop r <- stack
        add     6       4       4 			; -- stack
        lw      4       1       stack 		; pop n <- satck
        add     2       6       2 			; r -= 1
        lw      0       6       pos1 		; reg6 = 1
push    add     6       4       4 			; ++ stack
        add     6       4       4 			; ++ stack
        add     6       4       4   		; ++ stack
        lw      0       6       index 		; reg6 = index
        jalr    6       5 				    ; jump to check >> reg6 = index >> reg5 = return

        lw      0       6       neg1 		; reg6 = -1
pop     add     6       4       4 			; -- stack
        lw      4       7       stack 		; pop reg7 <- stack
        add     3       7       3 			; reg3 += reg7
        add     6       4       4 			; -- stack
        add     6       4       4 			; -- stack
        add     6       4       4 			; -- stack
        lw      4       5       stack 	    ; pop reg5 <- stack
        jalr    5       6 				    ; return result
        beq     0       0       done        ; halt

addone  lw      0       6       pos1  		; reg6 = 1
        add     0       6       3 			; reg3 += 1
        jalr    5       6 			        ; return result

done    halt 				                ; end

n           .fill       8
r           .fill       2
neg1        .fill       -1
pos1        .fill       1
index       .fill       check
return      .fill       done
stack       .fill       0                   ; stack beginning