# Contributing to ZeroHunger

Thank you for considering contributing to ZeroHunger! This document provides guidelines for contributing to the project.

## 🤝 How to Contribute

### Reporting Bugs

1. Check if the bug has already been reported in [Issues](https://github.com/NajmAlzorqah/zero-hunger/issues)
2. If not, create a new issue with:
   - Clear title and description
   - Steps to reproduce
   - Expected vs actual behavior
   - System information (OS, Java version, browser)
   - Screenshots if applicable

### Suggesting Features

1. Check existing [Issues](https://github.com/NajmAlzorqah/zero-hunger/issues) for similar suggestions
2. Create a new issue with:
   - Clear description of the feature
   - Use cases and benefits
   - Possible implementation approach

### Pull Requests

1. **Fork the repository**

   ```bash
   git clone https://github.com/YOUR_USERNAME/zero-hunger.git
   cd zero-hunger
   ```

2. **Create a feature branch**

   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make your changes**

   - Follow existing code style
   - Add comments for complex logic
   - Update documentation if needed
   - Test your changes thoroughly

4. **Commit your changes**

   ```bash
   git add .
   git commit -m "feat: add your feature description"
   ```

5. **Push to your fork**

   ```bash
   git push origin feature/your-feature-name
   ```

6. **Create a Pull Request**
   - Provide clear description of changes
   - Reference related issues
   - Include screenshots for UI changes

## 📋 Code Style Guidelines

### Backend (Java/Jakarta EE)

- Follow Java naming conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public methods
- Keep methods focused and concise
- Use proper exception handling

### Frontend (Next.js/React)

- Use functional components with hooks
- Follow React best practices
- Use meaningful component and variable names
- Keep components small and reusable
- Add comments for complex logic

## 🧪 Testing

- Write tests for new features
- Ensure existing tests pass before submitting PR
- Test both backend API and frontend UI changes

## 🔍 Code Review Process

1. All submissions require review
2. Reviewers may request changes
3. Address feedback promptly
4. Once approved, maintainers will merge

## 📝 Commit Message Guidelines

Use conventional commits format:

- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation changes
- `style:` Code style changes (formatting)
- `refactor:` Code refactoring
- `test:` Adding/updating tests
- `chore:` Maintenance tasks

Example: `feat: add email notification for new donations`

## 🎯 Development Setup

### Backend Setup

1. Install JDK 21
2. Install GlassFish 7.x or WildFly 27+
3. Setup MySQL database
4. Configure datasource
5. Build and deploy

See [zero-hunger-api/README.md](zero-hunger-api/README.md) for details.

### Frontend Setup

1. Install Node.js 18+
2. Install pnpm: `npm install -g pnpm`
3. Install dependencies: `pnpm install`
4. Configure environment variables
5. Run dev server: `pnpm dev`

See [zero-hunger-frontend/README.md](zero-hunger-frontend/FRONTEND-README.md) for details.

## 📜 License

By contributing, you agree that your contributions will be licensed under the MIT License.

## ❓ Questions?

Feel free to create an issue or reach out to the maintainers.

Thank you for contributing to ZeroHunger! 🌱
