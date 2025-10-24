; Example 6502 Assembly Code for DASM
; Demonstrates DASM-style subroutine definitions and label+RTS patterns
;
; DASM uses SUBROUTINE directive to mark subroutine boundaries
; Also supports simple label: ... RTS patterns

    PROCESSOR 6502
    ORG $8000

; Simple subroutine using label+RTS pattern
; Expected complexity: 1
clear_screen:
    LDA #$20        ; Space character
    LDX #$00
.loop:
    STA $0400,X
    STA $0500,X
    STA $0600,X
    STA $0700,X
    INX
    BNE .loop       ; This is a branch but not counted as separate subroutine
    RTS

; Subroutine with DASM SUBROUTINE directive
; Expected complexity: 3 (base 1 + 2 branches)
    SUBROUTINE
wait_key:
    LDA $C5
    CMP #$40
    BEQ wait_key    ; +1 complexity (wait loop)
    CMP #$20
    BCC invalid     ; +1 complexity
    RTS
invalid:
    LDA #$00
    RTS

; Game logic with multiple conditionals
; Expected complexity: 6 (base 1 + 5 branches)
    SUBROUTINE
check_collision:
    LDA sprite_x
    CMP player_x
    BNE no_collision    ; +1 complexity
    BCC no_collision    ; +1 complexity (different condition)

    LDA sprite_y
    CMP player_y
    BNE no_collision    ; +1 complexity
    BCC no_collision    ; +1 complexity

    ; Collision detected
    LDA lives
    BEQ game_over       ; +1 complexity
    DEC lives
    RTS

game_over:
    LDA #$00
    STA game_state
    RTS

no_collision:
    RTS

; Complex state machine
; Expected complexity: 7 (base 1 + 6 branches)
    SUBROUTINE
update_state:
    LDA game_state
    BEQ state_menu      ; +1 complexity
    CMP #$01
    BEQ state_playing   ; +1 complexity
    CMP #$02
    BEQ state_paused    ; +1 complexity
    CMP #$03
    BEQ state_gameover  ; +1 complexity
    ; Unknown state, reset
    LDA #$00
    STA game_state
    RTS

state_menu:
    ; Menu logic
    LDA input
    CMP #$01
    BEQ start_game      ; +1 complexity
    RTS

state_playing:
    ; Game logic
    JSR check_collision
    RTS

state_paused:
    ; Pause logic
    LDA input
    CMP #$01
    BEQ resume_game     ; +1 complexity
    RTS

state_gameover:
    ; Game over logic
    RTS

start_game:
    LDA #$01
    STA game_state
    RTS

resume_game:
    LDA #$01
    STA game_state
    RTS

; Data
sprite_x:   .byte $00
sprite_y:   .byte $00
player_x:   .byte $00
player_y:   .byte $00
lives:      .byte $03
game_state: .byte $00
input:      .byte $00
