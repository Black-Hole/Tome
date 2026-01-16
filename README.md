# Tome

A Minecraft procedural generation library written in Kotlin, providing tools for world generation, editing, and AI-assisted content creation.

## Features

- **Geometry**: Core spatial data structures and algorithms (vectors, blocks, regions, volumes)
- **Minecraft**: Native Minecraft data structure support (blocks, chunks, NBT, schematics)
- **World Generation**: Advanced noise-based terrain generation with multiple algorithms
- **World Editing**: Powerful tools for manipulating Minecraft worlds
- **HTTP Integration**: Client for interacting with Minecraft servers and APIs
- **AI Integration**: OpenAI-powered procedural generation assistance
- **Data Management**: Efficient caching and data access patterns
- **Configuration**: Flexible configuration system with environment variable support

## Requirements

- Java 17 or higher
- Maven 3.6 or higher

## Building

Build the project with Maven:

```bash
mvn clean compile
```

## Running Tests

Execute the test suite:

```bash
mvn test
```

## Creating a JAR

Package the library into an executable JAR with all dependencies:

```bash
mvn package
```

The JAR files will be created in the `target/` directory:
- `tome-0.1.0.jar` - Main JAR
- `tome-0.1.0-jar-with-dependencies.jar` - Fat JAR with all dependencies

## Dependencies

Key dependencies include:
- **Kotlin 1.9.22**: Primary language
- **Kotlinx Coroutines**: Async/await functionality
- **Ktor**: HTTP client
- **Arrow-kt**: Functional programming utilities
- **OpenAI Client**: AI integration
- **Adventure NBT**: NBT data handling
- **Fastutil**: High-performance collections
- **Caffeine**: Caching

See `pom.xml` for the complete dependency list.

## Project Structure

```
src/main/kotlin/
├── ai/          # AI integration for procedural generation
├── config/      # Configuration management
├── data/        # Data access and caching
├── editor/      # World editing operations
├── generator/   # World generation algorithms
├── geometry/    # Spatial data structures
├── http/        # HTTP client functionality
├── minecraft/   # Minecraft-specific structures
├── noise/       # Noise generation functions
└── util/        # Utility functions

data/            # Data files (biomes, structures, etc.)
```

## Configuration

The AI module requires an OpenAI API key. Create a `.env` file in the project root:

```env
OPENAI_API_KEY=your_api_key_here
```

## Usage Examples

### Generating Noise

```kotlin
import noise.PerlinNoise

val noise = PerlinNoise(seed = 12345L)
val value = noise.noise2D(x = 10.0, y = 20.0)
```

### Working with Geometry

```kotlin
import geometry.Vec3
import geometry.BlockPos

val position = BlockPos(x = 10, y = 64, z = -5)
val offset = position.offset(dx = 1, dy = 0, dz = 1)
```

### Using the Data Module

See [DATA_MODULE_USAGE.md](DATA_MODULE_USAGE.md) for detailed information about the data access system.

## CI/CD

The project uses GitHub Actions for continuous integration. The workflow:
1. Builds the project
2. Runs all tests
3. Packages the JAR
4. Uploads artifacts

## License

This project is a library for Minecraft procedural generation and world manipulation.

## Contributing

Contributions are welcome! Please ensure:
1. Code follows Kotlin conventions
2. All tests pass (`mvn test`)
3. New features include tests
4. Documentation is updated

## Development

To set up the development environment:

```bash
# Clone the repository
git clone <repository-url>
cd Tome

# Build the project
mvn clean install

# Run tests
mvn test
```

## Module Overview

- **ai**: Provides OpenAI integration for generating structures and content
- **config**: Environment-based configuration with .env support
- **data**: Caching layer and data access for biomes, structures, and regions
- **editor**: Tools for modifying Minecraft worlds (fill, replace, copy, paste)
- **generator**: World generation with biome-aware terrain generation
- **geometry**: Core spatial math (vectors, positions, regions, volumes)
- **http**: Async HTTP client built on Ktor
- **minecraft**: Block states, chunks, NBT, schematic files
- **noise**: Multiple noise algorithms (Perlin, Simplex, OpenSimplex2)
- **util**: Logging, serialization helpers, and utilities
