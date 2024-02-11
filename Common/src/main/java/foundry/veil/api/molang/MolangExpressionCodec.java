package foundry.veil.api.molang;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import gg.moonflower.molangcompiler.api.MolangEnvironment;
import gg.moonflower.molangcompiler.api.MolangEnvironmentBuilder;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.exception.MolangException;
import gg.moonflower.molangcompiler.api.exception.MolangRuntimeException;
import gg.moonflower.molangcompiler.api.object.MolangObject;

import java.util.Collection;

/**
 * Defines a codec for MoLang expressions.
 *
 * @author Ocelot
 */
public final class MolangExpressionCodec {

    public static final Codec<MolangExpression> CODEC = new Impl();

    private MolangExpressionCodec() {
    }

    private static class Impl implements Codec<MolangExpression> {

        private static final MolangEnvironment ENCODE_ENVIRONMENT = new EncodeEnvironment();

        @Override
        public <T> DataResult<T> encode(MolangExpression input, DynamicOps<T> ops, T prefix) {
            try {
                return DataResult.success(ops.createFloat(ENCODE_ENVIRONMENT.resolve(input)));
            } catch (MolangException ignored) {
                // The value cannot be encoded as a float
            }
            return DataResult.success(ops.createString(input.toString()));
        }

        @Override
        public <T> DataResult<Pair<MolangExpression, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<Number> numberValue = ops.getNumberValue(input);
            if (numberValue.result().isPresent()) {
                return DataResult.success(Pair.of(MolangExpression.of(numberValue.result().get().floatValue()), input));
            }

            DataResult<Boolean> booleanValue = ops.getBooleanValue(input);
            if (booleanValue.result().isPresent()) {
                return DataResult.success(Pair.of(MolangExpression.of(booleanValue.result().get()), input));
            }

            DataResult<String> stringValue = ops.getStringValue(input);
            if (stringValue.result().isPresent()) {
                try {
                    return DataResult.success(Pair.of(VeilMolang.get().compile(stringValue.result().get()), input));
                } catch (Exception e) {
                    return DataResult.error(e::getMessage);
                }
            }

            return DataResult.error(() -> "Not a number, boolean, or string: " + input);
        }
    }

    private static class EncodeEnvironment implements MolangEnvironment {

        @Override
        public void loadLibrary(String name, MolangObject object, String... aliases) {
            throw new UnsupportedOperationException("Invalid Call");
        }

        @Override
        public void loadAlias(String name, String first, String... aliases) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Invalid Call");
        }

        @Override
        public void loadParameter(float value) throws MolangRuntimeException {
            throw new MolangRuntimeException("Invalid Call");
        }

        @Override
        public void clearParameters() {
        }

        @Override
        public float getThis() {
            return 0;
        }

        @Override
        public MolangObject get(String name) throws MolangRuntimeException {
            throw new MolangRuntimeException("Invalid Call");
        }

        @Override
        public float getParameter(int parameter) throws MolangRuntimeException {
            throw new MolangRuntimeException("Invalid Call");
        }

        @Override
        public int getParameters() {
            return 0;
        }

        @Override
        public Collection<String> getObjects() {
            return null;
        }

        @Override
        public void setThisValue(float thisValue) {
        }

        @Override
        public boolean canEdit() {
            return false;
        }

        @Override
        public MolangEnvironmentBuilder<? extends MolangEnvironment> edit() throws IllegalStateException {
            throw new IllegalStateException("Immutable MoLang environments cannot be edited");
        }
    }
}
