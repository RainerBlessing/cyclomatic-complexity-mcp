; Simple x86 assembly example
section .text
global _start

_start:
    mov eax, 5
    cmp eax, 0
    je zero_case
    jg positive_case
    jmp end

zero_case:
    mov ebx, 0
    jmp end

positive_case:
    mov ebx, 1
    loop process_loop

process_loop:
    dec eax
    jnz process_loop

end:
    mov eax, 1
    int 0x80
