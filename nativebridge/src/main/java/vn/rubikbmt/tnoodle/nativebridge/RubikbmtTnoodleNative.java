package vn.rubikbmt.tnoodle.nativebridge;

import java.nio.charset.StandardCharsets;

import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.UnmanagedMemory;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.worldcubeassociation.tnoodle.scrambles.Puzzle;
import org.worldcubeassociation.tnoodle.scrambles.PuzzleRegistry;

public final class RubikbmtTnoodleNative {
    private RubikbmtTnoodleNative() {
    }

    public static void main(String[] args) {
    }

    @CEntryPoint(name = "tnoodle_lib_scramble")
    public static CCharPointer scramble(IsolateThread thread, int registryId) {
        try {
            return toCString(puzzleForRegistryId(registryId).generateScramble());
        } catch (RuntimeException e) {
            return toCString("");
        }
    }

    @CEntryPoint(name = "tnoodle_lib_draw_scramble")
    public static CCharPointer drawScramble(IsolateThread thread, int registryId, CCharPointer scramble) {
        try {
            Puzzle puzzle = puzzleForRegistryId(registryId);
            String scrambleText = scramble.isNull() ? "" : org.graalvm.nativeimage.c.type.CTypeConversion.toJavaString(scramble);
            return toCString(puzzle.drawScramble(scrambleText, null).toString());
        } catch (Exception e) {
            return toCString("");
        }
    }

    @CEntryPoint(name = "tnoodle_lib_free_string")
    public static void freeString(IsolateThread thread, CCharPointer value) {
        if (!value.isNull()) {
            UnmanagedMemory.free(value);
        }
    }

    private static Puzzle puzzleForRegistryId(int registryId) {
        switch (registryId) {
            case 0:
                return PuzzleRegistry.TWO.getScrambler();
            case 1:
                return PuzzleRegistry.THREE.getScrambler();
            case 2:
                return PuzzleRegistry.FOUR.getScrambler();
            case 8:
                return PuzzleRegistry.PYRA.getScrambler();
            case 10:
                return PuzzleRegistry.SKEWB.getScrambler();
            default:
                throw new IllegalArgumentException("Unsupported TNoodle registry id: " + registryId);
        }
    }

    private static CCharPointer toCString(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        CCharPointer output = UnmanagedMemory.malloc(bytes.length + 1);
        for (int index = 0; index < bytes.length; index++) {
            output.write(index, bytes[index]);
        }
        output.write(bytes.length, (byte) 0);
        return output;
    }
}
