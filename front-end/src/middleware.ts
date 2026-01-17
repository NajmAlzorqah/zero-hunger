import { NextResponse, type NextRequest } from "next/server";

import { AUTH_TOKEN_KEY } from "./lib/constants";

const publicRoutes = ["/", "/login", "/register"];
const authRoutes = ["/login", "/register"];

const isAuthRoute = (pathname: string): boolean => authRoutes.includes(pathname);
const isPublicRoute = (pathname: string): boolean => publicRoutes.includes(pathname);

// CORS headers for all responses
function addCorsHeaders(response: NextResponse): NextResponse {
  response.headers.set("Access-Control-Allow-Origin", "*");
  response.headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
  response.headers.set("Access-Control-Allow-Headers", "*");
  response.headers.set("Access-Control-Allow-Credentials", "true");
  response.headers.set("Access-Control-Max-Age", "86400");
  return response;
}

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // Handle OPTIONS preflight requests
  if (request.method === "OPTIONS") {
    const response = new NextResponse(null, { status: 200 });
    return addCorsHeaders(response);
  }

  const token = request.cookies.get(AUTH_TOKEN_KEY)?.value;

  const isPublic = isPublicRoute(pathname);
  const isAuthPage = isAuthRoute(pathname);
  const isProtected = !isPublic;

  if (!token && isProtected) {
    const loginUrl = new URL("/login", request.url);
    const response = NextResponse.redirect(loginUrl);
    return addCorsHeaders(response);
  }

  if (token && isAuthPage) {
    const dashboardUrl = new URL("/dashboard", request.url);
    const response = NextResponse.redirect(dashboardUrl);
    return addCorsHeaders(response);
  }

  const response = NextResponse.next();
  return addCorsHeaders(response);
}

export const config = {
  matcher: ["/((?!_next/static|_next/image|favicon.ico|images|.*\\.(?:svg|png|jpg|jpeg|gif|webp|ico)$).*)"],
};
