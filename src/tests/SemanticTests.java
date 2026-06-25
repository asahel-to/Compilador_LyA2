package tests;

import java.util.List;
import lexico.AFD;
import lexico.SortScriptAFD;
import lexico.Token;
import lexico.Tokenizador;
import parser.ParserLL1;
import parser.SemanticAnalyzer;
import parser.Errores;
import parser.ErrorCompilacion;

public class SemanticTests {
    public static void main(String[] args) {
        runAll();
    }

    private static void runAll() {
        int passed = 0, failed = 0;

        if (runTest("sem_ok", sampleOk(), 0)) passed++; else failed++;
        if (runTest("sem_undeclared", sampleUndeclared(), 1)) passed++; else failed++;
        if (runTest("sem_redeclare", sampleRedeclare(), 1)) passed++; else failed++;
        if (runTest("sem_for_scope", sampleForScope(), 1)) passed++; else failed++;
        if (runTest("sem_func_arity_fail", sampleFuncArityFail(), 1)) passed++; else failed++;
        if (runTest("sem_func_arg_type_fail", sampleFuncArgTypeFail(), 1)) passed++; else failed++;
        if (runTest("sem_func_ok", sampleFuncOk(), 0)) passed++; else failed++;
        if (runTest("sem_attr_ok", sampleAttrOk(), 0)) passed++; else failed++;
        if (runTest("sem_attr_on_number", sampleAttrOnNumber(), 1)) passed++; else failed++;
        if (runTest("sem_attr_undeclared", sampleAttrUndeclared(), 1)) passed++; else failed++;
        if (runTest("sem_if_scope", sampleIfScope(), 1)) passed++; else failed++;
        if (runTest("sem_else_scope", sampleElseScope(), 1)) passed++; else failed++;
        if (runTest("sem_else_if_scope", sampleElseIfScope(), 1)) passed++; else failed++;
        if (runTest("sem_log_ok_1", sampleLogOk1(), 0)) passed++; else failed++;
        if (runTest("sem_log_ok_2", sampleLogOk2(), 0)) passed++; else failed++;
        if (runTest("sem_log_ok_number", sampleLogOkNumber(), 0)) passed++; else failed++;
        if (runTest("sem_log_arity_fail_0", sampleLogArityFail0(), 1)) passed++; else failed++;
        if (runTest("sem_log_arity_fail_3", sampleLogArityFail3(), 1)) passed++; else failed++;
        if (runTest("sem_copy_ok", sampleCopyOk(), 0)) passed++; else failed++;
        if (runTest("sem_copy_type_fail", sampleCopyTypeFail(), 1)) passed++; else failed++;
        if (runTest("sem_rename_ok", sampleRenameOk(), 0)) passed++; else failed++;
        if (runTest("sem_rename_type_fail", sampleRenameTypeFail(), 1)) passed++; else failed++;
        if (runTest("sem_delete_ok", sampleDeleteOk(), 0)) passed++; else failed++;
        if (runTest("sem_move_case_insensitive", sampleMoveCaseInsensitive(), 0)) passed++; else failed++;
        if (runTest("sem_move_string_not_path", sampleMoveStringNotPath(), 1)) passed++; else failed++;
        if (runParserRecoveryTest("syn_missing_paren_in_move", sampleMissingParenInMove(), 1, 0)) passed++; else failed++;
        if (runParserRecoveryTest("syn_missing_brace_in_for", sampleMissingBraceInFor(), 1, 0)) passed++; else failed++;
        if (runParserRecoveryTest("syn_unexpected_brace_outer", sampleUnexpectedBraceOuter(), 1, 0)) passed++; else failed++;
        if (runTest("lex_url_in_string", sampleUrlInString(), 0)) passed++; else failed++;
        if (runTest("lex_comment_like_in_string", sampleCommentLikeInString(), 0)) passed++; else failed++;
        if (runTest("lex_tmp_as_identifier", sampleTmpAsIdentifier(), 0)) passed++; else failed++;
        if (runTest("lex_bak_as_identifier", sampleBakAsIdentifier(), 0)) passed++; else failed++;
        if (runTest("lex_ext_uppercase", sampleExtUppercase(), 0)) passed++; else failed++;
        if (runTest("lex_underscore_id", sampleUnderscoreId(), 0)) passed++; else failed++;
        if (runTest("lex_size_zero", sampleSizeZero(), 0)) passed++; else failed++;
        if (runTest("syn_empty_program_simple", sampleEmptyProgramSimple(), 0)) passed++; else failed++;
        if (runTest("syn_empty_for_body", sampleEmptyForBody(), 0)) passed++; else failed++;
        if (runParserRecoveryTest("syn_missing_scan", sampleMissingScan(), 1, 0)) passed++; else failed++;
        if (runTest("syn_nested_if_in_for", sampleNestedIfInFor(), 0)) passed++; else failed++;
        if (runTest("sem_for_source_undeclared", sampleForSourceUndeclared(), 1)) passed++; else failed++;
        if (runTest("sem_delete_case_insensitive", sampleDeleteCaseInsensitive(), 0)) passed++; else failed++;
        if (runTest("sem_copy_string_path_mix", sampleCopyStringPathMix(), 1)) passed++; else failed++;
        if (runTest("sem_log_variable_from_for", sampleLogVariableFromFor(), 0)) passed++; else failed++;
        if (runTest("sem_scope_inherit_outer", sampleScopeInheritOuter(), 0)) passed++; else failed++;

        System.out.println("\nSummary: passed=" + passed + " failed=" + failed);
        if (failed > 0) System.exit(2);
    }

    private static boolean runTest(String name, String code, int expectedSemErrors) {
        System.out.println("--- Test: " + name + " ---");
        try {
            Token[] raw = Tokenizador.tokenizador(code);
            AFD afd = SortScriptAFD.obtenerAFD();
            List<Token> tokens = afd.aceptar(raw);
            if (afd.getErrores().hayErrores()) {
                System.out.println("Lexical errors detected (failing test):");
                afd.getErrores().imprimirTabla();
                return false;
            }

            // filter tokens that parser expects
            java.util.ArrayList<Token> parserTokens = new java.util.ArrayList<>();
            for (Token t : tokens) if (t.existeSimbolo()) parserTokens.add(t);

            ParserLL1 p = new ParserLL1(parserTokens);
            p.inicio();
            if (p.errores.hayErrores()) {
                System.out.println("Syntactic errors detected (failing test):");
                p.errores.imprimirTabla();
                return false;
            }

            parser.NodoArbol raiz = p.getRaiz();
            parser.SemanticAnalyzer sem = new SemanticAnalyzer();
            sem.analizar(raiz);
            int semCount = sem.errores.getErrores().size();
            if (semCount != expectedSemErrors) {
                System.out.println("Expected " + expectedSemErrors + " semantic errors, found " + semCount);
                if (semCount > 0) sem.errores.imprimirTabla();
                return false;
            }
            System.out.println("OK (semantic errors=" + semCount + ")");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String sampleOk() {
        return "scan \"/home\" as files\n" +
               "for each item in files {\n" +
               "    let x = item.name\n" +
               "}\n" +
               "done()\n";
    }

    private static String sampleUndeclared() {
        return "scan \"/home\" as files\n" +
               "let x = y\n" +
               "done()\n";
    }

    private static String sampleRedeclare() {
        return "scan \"/home\" as files\n" +
               "let files = 0\n" +
               "done()\n";
    }

    private static String sampleForScope() {
        return "scan \"/home\" as files\n" +
               "for each x in files {\n" +
               "    let inner = x.name\n" +
               "}\n" +
               "let used = inner\n" +
               "done()\n";
    }

    private static String sampleFuncArityFail() {
        return "scan \"/home\" as files\n" +
               "move(files, \"/dest\", \"/extra\")\n" +
               "done()\n";
    }

    private static String sampleFuncArgTypeFail() {
        return "scan \"/home\" as files\n" +
               "delete(123)\n" +
               "done()\n";
    }

    private static String sampleFuncOk() {
        return "scan \"/home\" as files\n" +
               "for each x in files { move(x, \"/dest\") }\n" +
               "done()\n";
    }

    private static String sampleAttrOk() {
        return "scan \"/home\" as files\n" +
               "for each x in files { let z = x.ext }\n" +
               "done()\n";
    }

    private static String sampleAttrOnNumber() {
        return "scan \"/home\" as files\n" +
               "let k = 123\n" +
               "let x = k.ext\n" +
               "done()\n";
    }

    private static String sampleAttrUndeclared() {
        return "scan \"/home\" as files\n" +
               "let x = y.size\n" +
               "done()\n";
    }

    private static String sampleIfScope() {
        return "scan \"/home\" as files\n" +
               "if 1 == 1 {\n" +
               "    let inner = 10\n" +
               "}\n" +
               "let x = inner\n" +
               "done()\n";
    }

    private static String sampleElseScope() {
        return "scan \"/home\" as files\n" +
               "if 1 == 1 {\n" +
               "    let z = 1\n" +
               "} else {\n" +
               "    let b = 2\n" +
               "}\n" +
               "let x = b\n" +
               "done()\n";
    }

    private static String sampleElseIfScope() {
        return "scan \"/home\" as files\n" +
               "if 1 == 1 {\n" +
               "    let z = 1\n" +
               "} else if 2 == 2 {\n" +
               "    let b = 2\n" +
               "}\n" +
               "let x = b\n" +
               "done()\n";
    }

    private static String sampleLogOk1() {
        return "scan \"/home\" as files\n" +
               "log(\"mensaje\")\n" +
               "done()\n";
    }

    private static String sampleLogOk2() {
        return "scan \"/home\" as files\n" +
               "log(\"mensaje\", \"INFO\")\n" +
               "done()\n";
    }

    private static String sampleLogOkNumber() {
        return "scan \"/home\" as files\n" +
               "log(42)\n" +
               "done()\n";
    }

    private static String sampleLogArityFail0() {
        return "scan \"/home\" as files\n" +
               "log()\n" +
               "done()\n";
    }

    private static String sampleLogArityFail3() {
        return "scan \"/home\" as files\n" +
               "log(1, 2, 3)\n" +
               "done()\n";
    }

    private static String sampleCopyOk() {
        return "scan \"/home\" as files\n" +
               "for each x in files { copy(x, \"/dest\") }\n" +
               "done()\n";
    }

    private static String sampleCopyTypeFail() {
        return "scan \"/home\" as files\n" +
               "copy(123, \"/dest\")\n" +
               "done()\n";
    }

    private static String sampleRenameOk() {
        return "scan \"/home\" as files\n" +
               "for each x in files { rename(x, \"/dest\") }\n" +
               "done()\n";
    }

    private static String sampleRenameTypeFail() {
        return "scan \"/home\" as files\n" +
               "rename(99, \"/dest\")\n" +
               "done()\n";
    }

    private static String sampleDeleteOk() {
        return "scan \"/home\" as files\n" +
               "for each x in files { delete(x) }\n" +
               "done()\n";
    }

    private static String sampleMoveCaseInsensitive() {
        return "scan \"/home\" as files\n" +
               "for each x in files { MOVE(x, \"/dest\") }\n" +
               "done()\n";
    }

    private static String sampleMoveStringNotPath() {
        return "scan \"/home\" as files\n" +
               "move(456, \"/dest\")\n" +
               "done()\n";
    }

    // Parser error recovery tests:
    // Verify that cascade errors about done() are suppressed when earlier errors exist.

    private static boolean runParserRecoveryTest(String name, String code, int expectedSynErrors, int expectedSemErrors) {
        System.out.println("--- Test: " + name + " ---");
        try {
            Token[] raw = Tokenizador.tokenizador(code);
            AFD afd = SortScriptAFD.obtenerAFD();
            List<Token> tokens = afd.aceptar(raw);
            if (afd.getErrores().hayErrores()) {
                System.out.println("Lexical errors detected (failing test):");
                afd.getErrores().imprimirTabla();
                return false;
            }

            java.util.ArrayList<Token> parserTokens = new java.util.ArrayList<>();
            for (Token t : tokens) if (t.existeSimbolo()) parserTokens.add(t);

            ParserLL1 p = new ParserLL1(parserTokens);
            p.inicio();
            int synCount = p.errores.getErrores().size();
            if (synCount != expectedSynErrors) {
                System.out.println("Expected " + expectedSynErrors + " syntax errors, found " + synCount);
                if (synCount > 0) p.errores.imprimirTabla();
                return false;
            }

            parser.NodoArbol raiz = p.getRaiz();
            parser.SemanticAnalyzer sem = new SemanticAnalyzer();
            sem.analizar(raiz);
            int semCount = sem.errores.getErrores().size();
            if (semCount != expectedSemErrors) {
                System.out.println("Expected " + expectedSemErrors + " semantic errors, found " + semCount);
                if (semCount > 0) sem.errores.imprimirTabla();
                return false;
            }
            System.out.println("OK (syntax errors=" + synCount + ", semantic errors=" + semCount + ")");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String sampleMissingParenInMove() {
        return "scan \"/home\" as data\n" +
               "for each x in data { move(\"/path1\", \"/path2\" }\n" +
               "done()\n";
    }

    private static String sampleMissingBraceInFor() {
        return "scan \"/home\" as data\n" +
               "for each x in data {\n" +
               "    move(x, \"/dest\")\n" +
               "done()\n";
    }

    private static String sampleUnexpectedBraceOuter() {
        return "scan \"/home\" as data\n" +
               "move(\"/path1\", \"/path2\"\n" +
               "}\n" +
               "done()\n";
    }

    // Tarea 6: comment handling inside strings
    private static String sampleUrlInString() {
        return "scan \"/home\" as files\n" +
               "log(\"https://example.com\")\n" +
               "done()\n";
    }

    private static String sampleCommentLikeInString() {
        return "scan \"/home\" as files\n" +
               "log(\"/* not a comment */\")\n" +
               "done()\n";
    }

    // Tarea 6: extension words as identifiers
    private static String sampleTmpAsIdentifier() {
        return "scan \"/home\" as tmp\n" +
               "for each x in tmp {}\n" +
               "done()\n";
    }

    private static String sampleBakAsIdentifier() {
        return "scan \"/home\" as bak\n" +
               "for each x in bak {}\n" +
               "done()\n";
    }

    // Tarea 11: extension token case insensitivity
    private static String sampleExtUppercase() {
        return "scan \"/home\" as files\n" +
               "for each x in files {\n" +
               "    if x.ext == .PDF { move(x, \"/dest\") }\n" +
               "}\n" +
               "done()\n";
    }

    // Tarea 11: identifiers with underscores
    private static String sampleUnderscoreId() {
        return "scan \"/home\" as files\n" +
               "let my_var = 0\n" +
               "for each item in files { let x = my_var }\n" +
               "done()\n";
    }

    // Tarea 11: zero as size literal
    private static String sampleSizeZero() {
        return "scan \"/home\" as files\n" +
               "let threshold = 0MB\n" +
               "done()\n";
    }

    // Tarea 11: minimal valid program — scan then done, no statements
    private static String sampleEmptyProgramSimple() {
        return "scan \"/home\" as files\n" +
               "done()\n";
    }

    // Tarea 11: empty for body
    private static String sampleEmptyForBody() {
        return "scan \"/home\" as files\n" +
               "for each x in files {}\n" +
               "done()\n";
    }

    // Tarea 11: missing scan at start — syntax error
    private static String sampleMissingScan() {
        return "for each x in files {\n" +
               "    log(\"hello\")\n" +
               "}\n" +
               "done()\n";
    }

    // Tarea 11: nested if/else if/else inside a for body
    private static String sampleNestedIfInFor() {
        return "scan \"/home\" as files\n" +
               "for each x in files {\n" +
               "    if x.ext == .pdf {\n" +
               "        move(x, \"/docs\")\n" +
               "    } else if x.ext == .jpg {\n" +
               "        move(x, \"/images\")\n" +
               "    } else {\n" +
               "        log(x.name)\n" +
               "    }\n" +
               "}\n" +
               "done()\n";
    }

    // Tarea 11: for with undeclared source — 1 semantic error
    private static String sampleForSourceUndeclared() {
        return "scan \"/home\" as files\n" +
               "for each x in undefinedColl {\n" +
               "    log(x.name)\n" +
               "}\n" +
               "done()\n";
    }

    // Tarea 11: DELETE in uppercase — case insensitive, 0 errors
    private static String sampleDeleteCaseInsensitive() {
        return "scan \"/home\" as files\n" +
               "for each x in files { DELETE(x) }\n" +
               "done()\n";
    }

    // Tarea 11: copy with path + number — second argument type error
    private static String sampleCopyStringPathMix() {
        return "scan \"/home\" as files\n" +
               "copy(\"/dest\", 99)\n" +
               "done()\n";
    }

    // Tarea 11: log with variable from for body — 0 errors
    private static String sampleLogVariableFromFor() {
        return "scan \"/home\" as files\n" +
               "for each x in files {\n" +
               "    let msg = x.name\n" +
               "    log(msg)\n" +
               "}\n" +
               "done()\n";
    }

    // Tarea 11: outer-scope variable accessible inside blocks — 0 errors
    private static String sampleScopeInheritOuter() {
        return "scan \"/home\" as files\n" +
               "let prefix = \"FILE: \"\n" +
               "for each x in files {\n" +
               "    let msg = prefix + x.name\n" +
               "    log(msg)\n" +
               "}\n" +
               "done()\n";
    }
}
