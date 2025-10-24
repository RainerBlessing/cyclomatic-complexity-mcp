; Example x86-64 Assembly Code
; Demonstrates various control flow constructs

section .text
global main

; Simple function - Complexity: 1
add_numbers PROC
    mov eax, ecx
    add eax, edx
    ret
add_numbers ENDP

; Function with conditional - Complexity: 2
max_value PROC
    mov eax, ecx
    cmp ecx, edx
    jge done        ; conditional jump adds complexity
    mov eax, edx
done:
    ret
max_value ENDP

; Function with loop - Complexity: 3
sum_array PROC
    xor eax, eax    ; sum = 0
    xor ecx, ecx    ; i = 0
loop_start:
    cmp ecx, edx    ; compare i with length
    jge loop_end    ; conditional jump
    add eax, [rsi + rcx*4]
    inc ecx
    jmp loop_start  ; unconditional jump (no complexity)
loop_end:
    ret
sum_array ENDP

; More complex function - Complexity: 6
validate_input PROC
    test ecx, ecx
    jz invalid      ; +1

    cmp ecx, 100
    jg invalid      ; +1

    test edx, edx
    jz check_range  ; +1

    cmp edx, 50
    jl warning      ; +1
    jmp valid       ; unconditional

check_range:
    cmp ecx, 10
    jl invalid      ; +1

valid:
    mov eax, 1
    ret

warning:
    mov eax, 0
    ret

invalid:
    mov eax, -1
    ret
validate_input ENDP

; Function with loop instruction - Complexity: 2
repeat_action PROC
    mov ecx, 10
repeat_loop:
    call do_something
    loop repeat_loop    ; LOOP instruction adds complexity
    ret
repeat_action ENDP

do_something:
    ret
