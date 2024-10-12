        lw      0       2       start           ; โหลดค่า start เข้าสู่ reg2
        lw      0       3       final           ; โหลดค่า final เข้าสู่ reg3
        lw      0       5       neg1            ; โหลดค่า -1 เข้าสู่ reg4
        add     2       5       2               ; reg2 = start - 1 (ทำให้ค่าเริ่มต้นเป็น start - 1)

check   beq     3       2       done            ; ถ้า start เท่ากับ final - 1 ให้ออกจาก loop
        sub     6       3       6               ; reg6 = reg6 - final (ผลลัพธ์)
        add     3       5       3               ; ลดค่า final ลง 1
        beq     0       0       check           ; ทำต่อใน loop

done    halt                                    ; จบโปรแกรม

final   .fill   1000                             ; กำหนดค่า final เป็น 1000
start   .fill   0                                ; กำหนดค่า start เป็น 0
neg1    .fill   -1                               ; กำหนดค่า -1
